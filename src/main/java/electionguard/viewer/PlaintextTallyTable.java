/*
 * Copyright (c) 1998-2019 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package electionguard.viewer;

import electionguard.ballot.Manifest;
import electionguard.ballot.PlaintextTally;
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

public class PlaintextTallyTable extends JPanel {
  private final PreferencesExt prefs;

  private final BeanTable<PlaintextTallyBean> tallyTable;
  private final BeanTable<ContestBean> contestTable;
  private final BeanTable<SelectionBean> selectionTable;

  private final JSplitPane split1;
  private final JSplitPane split2;

  private final IndependentWindow infoWindow;

  private Manifest manifest;

  public PlaintextTallyTable(PreferencesExt prefs) {
    this.prefs = prefs;
    TextHistoryPane infoTA = new TextHistoryPane();
    infoWindow = new IndependentWindow("Extra Information", BAMutil.getImage("electionguard-logo.png"), infoTA);
    infoWindow.setBounds((Rectangle) prefs.getBean("InfoWindowBounds", new Rectangle(300, 300, 800, 100)));

    tallyTable = new BeanTable<>(PlaintextTallyBean.class, (PreferencesExt) prefs.node("TallyTable"), false,
            "PlaintextTally", "PlaintextTally", null);
    tallyTable.addListSelectionListener(e -> {
      PlaintextTallyBean tallyBean = tallyTable.getSelectedBean();
      if (tallyBean != null) {
        setTally(tallyBean);
      }
    });
    tallyTable.addPopupOption("Show Tally", tallyTable.makeShowAction(infoTA, infoWindow,
            bean -> ((PlaintextTallyBean)bean).tally.showTally()));

    contestTable = new BeanTable<>(ContestBean.class, (PreferencesExt) prefs.node("ContestTable"), false,
            "Contest", "PlaintextTally.Contest", null);
    contestTable.addListSelectionListener(e -> {
      ContestBean contest = contestTable.getSelectedBean();
      if (contest != null) {
        setContest(contest);
      }
    });
    contestTable.addPopupOption("Show Contest", contestTable.makeShowAction(infoTA, infoWindow,
            bean -> bean.toString()));

    selectionTable = new BeanTable<>(SelectionBean.class, (PreferencesExt) prefs.node("SelectionTable"), false,
            "Selection", "PlaintextTally.Selection", null);
    selectionTable.addPopupOption("Show Selection", selectionTable.makeShowAction(infoTA, infoWindow,
            bean -> bean.toString()));

    // layout
    split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, tallyTable, contestTable);
    split1.setDividerLocation(prefs.getInt("splitPos1", 200));

    split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, split1, selectionTable);
    split2.setDividerLocation(prefs.getInt("splitPos2", 200));

    setLayout(new BorderLayout());
    add(split2, BorderLayout.CENTER);
  }

  void setPlaintextTallies(Manifest manifest, Iterable<PlaintextTally> tallies) {
    this.manifest = manifest;
    java.util.List<PlaintextTallyBean> beanList = new ArrayList<>();
    for (PlaintextTally tally: tallies) {
        beanList.add(new PlaintextTallyBean(tally));
      }
      tallyTable.setBeans(beanList);
      if (beanList.size() > 0) {
        setTally(beanList.get(0));
      } else {
        contestTable.setBeans(new ArrayList<>());
        selectionTable.setBeans(new ArrayList<>());
      }
  }

  void setTally(PlaintextTallyBean plaintextTallyBean) {
    java.util.List<ContestBean> beanList = new ArrayList<>();
    for (PlaintextTally.Contest c : plaintextTallyBean.tally.getContests().values()) {
      beanList.add(new ContestBean(c));
    }
    contestTable.setBeans(beanList);
    if (beanList.size() > 0) {
      setContest(beanList.get(0));
    } else {
      selectionTable.setBeans(new ArrayList<>());
    }
  }

  void setContest(ContestBean contestBean) {
    selectionTable.setBeans(contestBean.selectionBeans);
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
  }

  public static class PlaintextTallyBean {
    PlaintextTally tally;

    public PlaintextTallyBean(){}

    PlaintextTallyBean(PlaintextTally tally) {
      this.tally = tally;
    }

    public String getId() {
      return tally.getTallyId();
    }
  }

  public class ContestBean {
    PlaintextTally.Contest contest;
    Manifest.ContestDescription mcontest;
    ArrayList<SelectionBean> selectionBeans = new ArrayList<>();

    public ContestBean(){}

    ContestBean(PlaintextTally.Contest contest) {
      this.contest = contest;
      this.mcontest = manifest.getContests().stream().filter(it -> it.getContestId().equals(contest.getContestId())).findFirst().orElseThrow();

      for (PlaintextTally.Selection s : contest.getSelections().values()) {
        selectionBeans.add(new SelectionBean(s, mcontest));
      }
    }
    public String getContestId() {
      return contest.getContestId();
    }

    public String getName() {
      return mcontest.getName();
    }

    public String getVoteVariation() {
      return mcontest.getVoteVariation().toString();
    }

    public String getVotes() {
      java.util.List<SelectionBean> sorted = selectionBeans.stream()
              .sorted((s1, s2) -> s2.getTally() - s1.getTally()).toList();

      Formatter f = new Formatter();
      for (SelectionBean sbean : sorted) {
        f.format("%s:%d, ", sbean.getCandidateId(), sbean.getTally());
      }
      return f.toString();
    }

    public String getWinner() {
      java.util.List<SelectionBean> sorted = selectionBeans.stream()
              .sorted((s1, s2) -> s2.getTally() - s1.getTally()).toList();

      SelectionBean winner = sorted.get(0);
      return winner.getCandidateId();
    }

    @Override
    public String toString() {
      return String.format("PlaintextTally.Contest%n %s%n%nManifest.ContestDescription%n%s%n",
              contest , mcontest.toString());
    }
  }

  public class SelectionBean {
    PlaintextTally.Selection selection;
    Manifest.SelectionDescription mselection;

    public SelectionBean(){}

    SelectionBean(PlaintextTally.Selection selection, Manifest.ContestDescription mcontest) {
      this.selection = selection;
      this.mselection = mcontest.getSelections().stream().filter(it -> it.getSelectionId().equals(selection.getSelectionId())).findFirst().orElseThrow();
    }

    public String getSelectionId() {
      return selection.getSelectionId();
    }

    public String getCandidateId() {
      return mselection.getCandidateId();
    }

    public int getTally() {
      return selection.getTally();
    }

    public int getNShares() {
      return selection.getPartialDecryptions().size();
    }

    @Override
    public String toString() {
      return String.format("PlaintextTally.Selection%n %s%n%nManifest.SelectionDescription%n%s%n",
              selection , mselection.toString());
    }

  }

}