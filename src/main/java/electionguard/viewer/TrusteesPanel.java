package electionguard.viewer;

import electionguard.core.GroupContext;
import electionguard.decrypt.DecryptingTrustee;
import ucar.ui.prefs.ComboBox;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.FileManager;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Formatter;

import static electionguard.publish.ReaderKt.readTrustee;

class TrusteesPanel extends JPanel {
  final PreferencesExt prefs;
  final TextHistoryPane ta;
  final IndependentWindow infoWindow;
  final ComboBox<String> trusteeDirCB;
  final JPanel topPanel;
  final JPanel buttPanel = new JPanel();
  final FileManager fileChooser;
  final DecryptingTrusteeTable trusteesDecryptingTable;
  GroupContext group = KUtils.productionGroup();

  boolean eventOk = true;
  String trusteeDir = "none";

  TrusteesPanel(PreferencesExt prefs, JFrame frame) {
    this.prefs = prefs;

    ////// Choose the inputBallotDir
    this.fileChooser = new FileManager(frame, null, null, (PreferencesExt) prefs.node("FileManager"));
    this.trusteeDirCB = new ComboBox<>((PreferencesExt) prefs.node("inputDirCB"));
    this.trusteeDirCB.addChangeListener(e -> {
      if (!this.eventOk) {
        return;
      }
      String trusteeDir = (String) trusteeDirCB.getSelectedItem();
      if (!trusteeDir.equals(this.trusteeDir)) {
        if (setTrusteeDir(trusteeDir)) {
          this.eventOk = false;
          this.trusteeDirCB.addItem(trusteeDir);
          this.eventOk = true;
          this.trusteeDir = trusteeDir;
        }
      }
    });
    AbstractAction fileAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String dirName = fileChooser.chooseFileOrDirectory(null);
        if (dirName != null) {
          trusteeDirCB.setSelectedItem(dirName);
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
    this.topPanel.add(trusteeDirCB, BorderLayout.CENTER);
    this.topPanel.add(buttPanel, BorderLayout.EAST);
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.addTab("DecryptingTrustees", this.trusteesDecryptingTable);
    tabbedPane.setSelectedIndex(0);
    add(tabbedPane, BorderLayout.CENTER);
  }

  boolean setTrusteeDir(String trusteeDir) {
    trusteesDecryptingTable.clearBeans();
    java.util.List<DecryptingTrustee> trustees = new ArrayList<>();
    for (String trusteeFile : trusteeFiles(trusteeDir)) {
      DecryptingTrustee trustee = readTrustee(group, trusteeDir + "/" +trusteeFile);
      if (trustee != null) {
        trustees.add(trustee);
      }
    }
    trusteesDecryptingTable.setTrustees(trustees);
    return true;
  }

  private String[] trusteeFiles(String trusteeDir) {
    Path trusteePath = Path.of(trusteeDir);
    if (!Files.exists(trusteePath) || !Files.isDirectory(trusteePath)) {
      throw new RuntimeException("Trustee dir '" + trusteeDir + "' does not exist");
    }
    return trusteePath.toFile().list();
  }

  void showInfo(Formatter f) {
    f.format("%s%n", this.trusteeDir);
    trusteesDecryptingTable.showInfo(f);
  }

  void save() {
    fileChooser.save();
    trusteeDirCB.save();
    trusteesDecryptingTable.save();
    if (infoWindow != null) {
      prefs.putBeanObject(ViewerMain.FRAME_SIZE, infoWindow.getBounds());
    }
  }
}
