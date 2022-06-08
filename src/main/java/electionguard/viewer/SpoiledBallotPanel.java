package electionguard.viewer;

import electionguard.ballot.Manifest;
import electionguard.ballot.PlaintextTally;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.FileManager;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Formatter;

class SpoiledBallotPanel extends JPanel {
  final PreferencesExt prefs;
  final TextHistoryPane ta;
  final IndependentWindow infoWindow;
  final JPanel topPanel;
  final JPanel buttPanel = new JPanel();
  final FileManager fileChooser;
  final PlaintextTallyTable spoiledBallotsTable;

  boolean eventOk = true;

  SpoiledBallotPanel(PreferencesExt prefs, JFrame frame) {
    this.prefs = prefs;

    ////// Choose the inputBallotDir
    this.fileChooser = new FileManager(frame, null, null, (PreferencesExt) prefs.node("FileManager"));

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
    BAMutil.setActionProperties(infoAction, "Information", "info on current Manifest Record", false, 'I', -1);
    BAMutil.addActionToContainer(buttPanel, infoAction);

    // components
    this.spoiledBallotsTable = new PlaintextTallyTable((PreferencesExt) prefs.node("InputBallots"));

    // layout
    this.topPanel = new JPanel(new BorderLayout());
    this.topPanel.add(buttPanel, BorderLayout.EAST);
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.addTab("SpoiledBallots", this.spoiledBallotsTable);
    tabbedPane.setSelectedIndex(0);
    add(tabbedPane, BorderLayout.CENTER);
  }

  boolean setSpoiledBallots(Manifest manifest, Iterable<PlaintextTally> ballots) {
    spoiledBallotsTable.setPlaintextTallies(manifest, ballots);
    return true;
  }

  void showInfo(Formatter f) {
  }

  void save() {
    fileChooser.save();

    spoiledBallotsTable.save();

    if (infoWindow != null) {
      prefs.putBeanObject(ViewerMain.FRAME_SIZE, infoWindow.getBounds());
    }
  }
}
