package electionguard.viewer;

import electionguard.ballot.Guardian;
import electionguard.core.GroupContext;
import electionguard.decrypt.DecryptingTrusteeIF;
import electionguard.publish.Consumer;
import ucar.ui.prefs.ComboBox;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.FileManager;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Formatter;

import static java.util.Collections.emptyList;

class TrusteesPanel extends JPanel {
  final PreferencesExt prefs;
  final TextHistoryPane ta;
  final IndependentWindow infoWindow;
  final ComboBox<String> inputBallotDirCB;
  final JPanel topPanel;
  final JPanel buttPanel = new JPanel();
  final FileManager fileChooser;
  final DecryptingTrusteeTable trusteesDecryptingTable;

  boolean eventOk = true;
  String inputFile = "none";

  TrusteesPanel(PreferencesExt prefs, JFrame frame) {
    this.prefs = prefs;

    ////// Choose the inputBallotDir
    this.fileChooser = new FileManager(frame, null, null, (PreferencesExt) prefs.node("FileManager"));
    this.inputBallotDirCB = new ComboBox<>((PreferencesExt) prefs.node("inputDirCB"));
    this.inputBallotDirCB.addChangeListener(e -> {
      if (!this.eventOk) {
        return;
      }
      this.inputFile = (String) inputBallotDirCB.getSelectedItem();
      if (setTrusteeDir(this.inputFile, null, emptyList())) {
        this.eventOk = false;
        this.inputBallotDirCB.addItem(this.inputFile);
        this.eventOk = true;
      }
    });
    AbstractAction fileAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String dirName = fileChooser.chooseFileOrDirectory(null);
        if (dirName != null) {
          inputBallotDirCB.setSelectedItem(dirName);
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
    BAMutil.setActionProperties(infoAction, "Information", "info on seleced Trustee Record", false, 'I', -1);
    BAMutil.addActionToContainer(buttPanel, infoAction);

    // components
    this.trusteesDecryptingTable = new DecryptingTrusteeTable((PreferencesExt) prefs.node("DecryptingTrustees"));

    // layout
    this.topPanel = new JPanel(new BorderLayout());
    this.topPanel.add(new JLabel("file:"), BorderLayout.WEST);
    this.topPanel.add(inputBallotDirCB, BorderLayout.CENTER);
    this.topPanel.add(buttPanel, BorderLayout.EAST);
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.addTab("DecryptingTrustees", this.trusteesDecryptingTable);
    tabbedPane.setSelectedIndex(0);
    add(tabbedPane, BorderLayout.CENTER);
  }

  boolean setTrusteeDir(String trusteeDir, GroupContext group, java.util.List<Guardian> guardians) {
    trusteesDecryptingTable.clearBeans();
    java.util.List<DecryptingTrusteeIF> trustees = new ArrayList<>();
    Consumer consumer = new Consumer(trusteeDir, group);
    for (Guardian guardian : guardians) {
      DecryptingTrusteeIF trustee = consumer.readTrustee(inputFile, guardian.getGuardianId());
      if (trustee != null) {
        trustees.add(trustee);
      }
    }
    trusteesDecryptingTable.setTrustees(trustees);
    return true;
  }

  void showInfo(Formatter f) {
    f.format("%s%n", this.inputFile);
    trusteesDecryptingTable.showInfo(f);
  }

  void save() {
    fileChooser.save();
    inputBallotDirCB.save();
    trusteesDecryptingTable.save();
    if (infoWindow != null) {
      prefs.putBeanObject(ViewerMain.FRAME_SIZE, infoWindow.getBounds());
    }
  }
}
