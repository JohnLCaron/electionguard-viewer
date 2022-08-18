package electionguard.viewer;

import electionguard.ballot.DecryptingGuardian;
import electionguard.ballot.ElectionConstants;
import electionguard.ballot.EncryptedBallot;
import electionguard.ballot.Manifest;
import electionguard.core.GroupContext;
import electionguard.json.JsonConsumer;
import electionguard.publish.Consumer;
import electionguard.publish.ElectionRecord;
import electionguard.verifier.Verifier;
import electionguard.ballot.Guardian;
import ucar.ui.prefs.ComboBox;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.FileManager;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.util.Formatter;
import java.util.stream.StreamSupport;

import static electionguard.publish.ElectionRecordFactoryKt.electionRecordFromConsumer;

class ElectionRecordPanel extends JPanel {
  private final PreferencesExt prefs;
  private final JPanel buttPanel = new JPanel();

  TextHistoryPane ta;
  IndependentWindow infoWindow;
  ComboBox<String> electionRecordDirCB;
  JPanel topPanel;

  FileManager fileChooser;
  boolean eventOk = true;

  String electionRecordDir = "none";
  GroupContext group = KUtils.productionGroup();
  ElectionRecord record;

  ManifestTable manifestTable;
  EncryptedBallotsTable submittedBallotsTable;
  PlaintextTallyTable plaintextTallyTable;
  EncryptedTallyTable ciphertextTallyTable;
  PlaintextTallyTable spoiledBallotsTable;

  ElectionRecordPanel(PreferencesExt prefs, JFrame frame) {
    this.prefs = prefs;

    ////// Choose the electionRecordDir
    this.fileChooser = new FileManager(frame, null, null, (PreferencesExt) prefs.node("FileManager"));
    this.electionRecordDirCB = new ComboBox<>((PreferencesExt) prefs.node("electionRecordDirCB"));
    this.electionRecordDirCB.addChangeListener(e -> {
      if (!this.eventOk) {
        return;
      }
      this.electionRecordDir = (String) electionRecordDirCB.getSelectedItem();
      if (setElectionRecord(this.electionRecordDir)) {
        this.eventOk = false;
        this.electionRecordDirCB.addItem(this.electionRecordDir);
        this.eventOk = true;
      }
    });
    AbstractAction fileAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String dirName = fileChooser.chooseDirectory("");
        if (dirName != null) {
          electionRecordDirCB.setSelectedItem(dirName);
        }
      }
    };
    BAMutil.setActionProperties(fileAction, "FileChooser", "open Local dataset...", false, 'L', -1);
    BAMutil.addActionToContainer(buttPanel, fileAction);

    // Popup info window
    this.ta = new TextHistoryPane(true);
    this.infoWindow = new IndependentWindow("Details", BAMutil.getImage("electionguard-logo.png"), new JScrollPane(ta));
    Rectangle bounds = (Rectangle) prefs.getBean(ViewerMain.FRAME_SIZE, new Rectangle(200, 50, 500, 700));
    this.infoWindow.setBounds(bounds);
    AbstractAction infoAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Formatter f = new Formatter();
        showInfo(f);
        ta.setText(f.toString());
        infoWindow.show();
      }
    };
    BAMutil.setActionProperties(infoAction, "Information", "info on Election Record", false, 'I', -1);
    BAMutil.addActionToContainer(buttPanel, infoAction);

    AbstractAction verifyAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Formatter f = new Formatter();
        verify(f);
        ta.setText(f.toString());
        infoWindow.show();
      }
    };
    BAMutil.setActionProperties(verifyAction, "Dump", "Verify Election Record", false, 'V', -1);
    BAMutil.addActionToContainer(buttPanel, verifyAction);

    // components
    this.manifestTable = new ManifestTable((PreferencesExt) prefs.node("Manifest"))
            .addActions(buttPanel);
    this.submittedBallotsTable = new EncryptedBallotsTable((PreferencesExt) prefs.node("CastBallots"));
    this.ciphertextTallyTable = new EncryptedTallyTable((PreferencesExt) prefs.node("CiphertextTally"));
    this.plaintextTallyTable = new PlaintextTallyTable((PreferencesExt) prefs.node("PlaintextTally"));
    this.spoiledBallotsTable = new PlaintextTallyTable((PreferencesExt) prefs.node("SpoiledBallots"));

    // layout
    this.topPanel = new JPanel(new BorderLayout());
    this.topPanel.add(new JLabel("dir:"), BorderLayout.WEST);
    this.topPanel.add(electionRecordDirCB, BorderLayout.CENTER);
    this.topPanel.add(buttPanel, BorderLayout.EAST);
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.addTab("Manifest", this.manifestTable);
    tabbedPane.addTab("EncryptedBallots", this.submittedBallotsTable);
    tabbedPane.addTab("EncryptedTally", this.ciphertextTallyTable);
    tabbedPane.addTab("DecryptedTally", this.plaintextTallyTable);
    tabbedPane.addTab("SpoiledBallotTallies", this.spoiledBallotsTable);
    tabbedPane.setSelectedIndex(0);
    add(tabbedPane, BorderLayout.CENTER);
  }

  boolean setElectionRecord(String electionRecordLocation) {
    try {
      JsonConsumer json = new JsonConsumer(electionRecordLocation);
      if (json.isValidElectionRecord(new Formatter())) {
        this.record = json.readElectionRecord();
      } else {
        this.record = electionRecordFromConsumer(new Consumer(electionRecordLocation, group));
      }

      manifestTable.setElectionManifest(record.manifest());

      Iterable<EncryptedBallot> ballots = record.encryptedBallots( null );
      submittedBallotsTable.setAcceptedBallots(record, ballots);
      if (record.encryptedTally() != null) {
        ciphertextTallyTable.setCiphertextTally(record.encryptedTally());
      }
      if (record.decryptedTally() != null) {
        plaintextTallyTable.setPlaintextTallies(record.manifest(), java.util.List.of(record.decryptedTally()));
      }
      spoiledBallotsTable.setPlaintextTallies(record.manifest(), record.spoiledBallotTallies());
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, e.getMessage());
    }
    return true;
  }

  private class MyFilter {
    public Boolean filter(electionguard.ballot.EncryptedBallot ballot) {
      return true;
    }
  }

  void showInfo(Formatter f) {
    f.format("Election Record %s%n", this.electionRecordDir);
    if (this.record != null) {
      f.format("  protoVersion = %s%n", record.protoVersion());
      Manifest manifest = record.manifest();
      f.format("%nManifest%n");
      f.format("  spec_version = %s%n", manifest.getSpecVersion());
      f.format("  election_scope_id = %s%n", manifest.getElectionScopeId());
      f.format("  type = %s%n", manifest.getElectionType());
      f.format("  name = %s%n", manifest.getName());
      f.format("  start_date = %s%n", manifest.getStartDate());
      f.format("  end_date = %s%n", manifest.getEndDate());
      f.format("  manifest crypto hash = %s%n", manifest.getCryptoHash());

      ElectionConstants constants = record.constants();
      f.format("%nConstants%n");
      f.format("  name = %s%n", constants.getName());
      f.format("  large_prime = %s%n", new BigInteger(1, constants.getLargePrime()));
      f.format("  small_prime = %s%n", new BigInteger(1, constants.getSmallPrime()));
      f.format("  cofactor    = %s%n", new BigInteger(1, constants.getCofactor()));
      f.format("  generator   = %s%n", new BigInteger(1, constants.getGenerator()));

      f.format("%nContext%n");
      f.format("  number_of_guardians = %s%n", record.numberOfGuardians());
      f.format("  quorum = %s%n", record.quorum());
      if (record.jointPublicKey() != null) {
        f.format("  election public key = %s%n", record.jointPublicKey().toStringShort());
      }
      f.format("  base hash = %s%n", record.cryptoBaseHash());
      f.format("  extended base hash = %s%n", record.cryptoExtendedBaseHash());

      /*
      f.format("%n  EncryptionDevices%n");
      for (Encrypt.EncryptionDevice device : record.devices) {
        f.format("    %d session=%d launch=%d location=%s%n", device.deviceId(), device.sessionId(), device.launchCode(), device.location());
      } */

      f.format("%n  Guardian Records: id, sequence #commitments #proofs%n");
      for (Guardian gr : record.guardians()) {
        f.format("    %10s %10d %10d %10d%n", gr.getGuardianId(), gr.getXCoordinate(),
                gr.getCoefficientCommitments().size(), gr.getCoefficientProofs().size());
      }

      f.format("%n  Available Guardians    lagrange%n");
      for (DecryptingGuardian guardian : record.decryptingGuardians()) {
        f.format("    %10s %10d %10s%n", guardian.getGuardianId(), guardian.getXCoordinate(), guardian.getLagrangeCoordinate());
      }

      f.format("%nEncryptedBallots %d%n", sizeof(record.encryptedBallots(null)));
      f.format("SpoiledBallotTallies %d%n", sizeof(record.spoiledBallotTallies()));

      f.format("%nMetadata%n");
      f.format("ElectionConfig present = %s%n", record.config() != null);
      if (record.config() != null) {
        f.format("   %s%n", record.config().getMetadata());
      }
      f.format("ElectionInit present = %s%n", record.electionInit() != null);
      if (record.electionInit() != null) {
        f.format("   %s%n", record.electionInit().getMetadata());
      }
      f.format("EncryptedTally present = %s%n", record.encryptedTally() != null);
      if (record.encryptedTally() != null) {
        f.format("   %s%n", record.tallyResult().getMetadata());
      }
      f.format("DecryptedTally present = %s%n", record.decryptedTally() != null);
      if (record.decryptedTally() != null) {
        f.format("   %s%n", record.decryptionResult().getMetadata());
      }
    }
  }

  long sizeof(Iterable<?> iter) {
    return StreamSupport.stream(iter.spliterator(), false).count();
  }

  void verify(Formatter f) {
    if (record == null) {
      return;
    }
    f.format(" Verify ElectionRecord from %s%n", this.record.topdir());
    Verifier verifier = new Verifier(record, 11);
    boolean ok = verifier.verify(true);
    f.format(" OK =  %s%n", ok);
  }


  void save() {
    fileChooser.save();
    electionRecordDirCB.save();

    manifestTable.save();
    submittedBallotsTable.save();
    plaintextTallyTable.save();
    ciphertextTallyTable.save();
    spoiledBallotsTable.save();

    if (infoWindow != null) {
      prefs.putBeanObject(ViewerMain.FRAME_SIZE, infoWindow.getBounds());
    }
  }
}
