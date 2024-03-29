package electionguard.viewer;

import electionguard.ballot.EncryptedTally;
import ucar.ui.prefs.BeanTable;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;

public class EncryptedTallyTable extends JPanel {
  private final PreferencesExt prefs;

  private final BeanTable<CiphertextTallyBean> tallyTable;
  private final BeanTable<ContestBean> contestTable;
  private final BeanTable<SelectionBean> selectionTable;

  private final JSplitPane split1, split2;
  private final IndependentWindow infoWindow;

  public EncryptedTallyTable(PreferencesExt prefs) {
    this.prefs = prefs;
    TextHistoryPane infoTA = new TextHistoryPane();
    infoWindow = new IndependentWindow("Extra Information", BAMutil.getImage("electionguard-logo.png"), infoTA);
    infoWindow.setBounds((Rectangle) prefs.getBean("InfoWindowBounds", new Rectangle(300, 300, 800, 100)));

    tallyTable = new BeanTable<>(CiphertextTallyBean.class, (PreferencesExt) prefs.node("TallyTable"), false,
            "CiphertextTally", "encrypted_tally", null);
    tallyTable.addPopupOption("Show Tally", tallyTable.makeShowAction(infoTA, infoWindow,
            bean -> ((CiphertextTallyBean)bean).tally.toString()));

    contestTable = new BeanTable<>(ContestBean.class, (PreferencesExt) prefs.node("ContestTable"), false,
            "Contest", "CiphertextTally.Contest", null);
    contestTable.addListSelectionListener(e -> {
      ContestBean contest = contestTable.getSelectedBean();
      if (contest != null) {
        setContest(contest);
      }
    });
    contestTable.addPopupOption("Show Contest", contestTable.makeShowAction(infoTA, infoWindow,
            bean -> ((ContestBean)bean).contest.toString()));

    selectionTable = new BeanTable<>(SelectionBean.class, (PreferencesExt) prefs.node("SelectionTable"), false,
            "Selection", "CiphertextTally.Selection", null);
    selectionTable.addPopupOption("Show Selection", selectionTable.makeShowAction(infoTA, infoWindow,
            bean -> ((SelectionBean)bean).selection.toString()));

    // layout
    split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, tallyTable, contestTable);
    split1.setDividerLocation(prefs.getInt("splitPos1", 200));

    split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, split1, selectionTable);
    split2.setDividerLocation(prefs.getInt("splitPos2", 200));

    setLayout(new BorderLayout());
    add(split2, BorderLayout.CENTER);
  }

  void setCiphertextTally(EncryptedTally plaintextTally) {
    tallyTable.setBeans(java.util.List.of(new CiphertextTallyBean(plaintextTally)));

    java.util.List<ContestBean> beanList = new ArrayList<>();
    for (EncryptedTally.Contest c : plaintextTally.getContests()) {
      beanList.add(new ContestBean(c));
    }
    contestTable.setBeans(beanList);
    selectionTable.setBeans(new ArrayList<>());
  }

  void setContest(ContestBean contestBean) {
    java.util.List<SelectionBean> beanList = new ArrayList<>();
    for (EncryptedTally.Selection s : contestBean.contest.getSelections()) {
      beanList.add(new SelectionBean(s));
    }
    selectionTable.setBeans(beanList);
  }

  void save() {
    tallyTable.saveState(false);
    contestTable.saveState(false);
    selectionTable.saveState(false);
    prefs.putBeanObject("InfoWindowBounds", infoWindow.getBounds());
    prefs.putInt("splitPos1", split1.getDividerLocation());
    prefs.putInt("splitPos2", split2.getDividerLocation());
  }

  void showInfo(Formatter f) {
    f.format(" Current time =   %s%n%n", new Date().toString());

    /* int n = 0;
    if (completeLogs != null) {
      n = completeLogs.size();
      f.format("Complete logs n=%d%n", n);
      f.format("  first log date= %s%n", completeLogs.get(0).getDate());
      f.format("   last log date= %s%n", completeLogs.get(n - 1).getDate());
    }
    List restrict = mergeTable.getBeans();
    if (restrict != null && (restrict.size() != n)) {
      f.format("%nRestricted, merged logs n=%d%n", restrict.size());
    }

    if (logFiles != null) {
      f.format("%nFiles used%n");
      for (LogLocalManager.FileDateRange fdr : logFiles) {
        f.format(" %s [%s,%s]%n", fdr.f.getName(), fdr.start, fdr.end);
      }
    } */
  }

  public class CiphertextTallyBean {
    EncryptedTally tally;

    public CiphertextTallyBean(){}

    CiphertextTallyBean(EncryptedTally tally) {
      this.tally = tally;
    }

    public String getId() {
      return tally.getTallyId();
    }
  }

  public class ContestBean {
    EncryptedTally.Contest contest;

    public ContestBean(){}

    ContestBean(EncryptedTally.Contest contest) {
      this.contest = contest;
    }
    public String getContestId() {
      return contest.getContestId();
    }
    public String getContestDescriptionHash() {
      return contest.getContestDescriptionHash().toString();
    }
  }

  public class SelectionBean {
    EncryptedTally.Selection selection;

    public SelectionBean(){}

    SelectionBean(EncryptedTally.Selection selection) {
      this.selection = selection;
    }

    public String getSelectionId() {
      return selection.getSelectionId();
    }

    public String getDescriptionHash() {
      return selection.getSelectionDescriptionHash().toString();
    }

    public String getCiphertextPad() {
      return selection.getCiphertext().getPad().toStringShort();
    }

    public String getCiphertextData() {
      return selection.getCiphertext().getData().toStringShort();
    }

  }

}
