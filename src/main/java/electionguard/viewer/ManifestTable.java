/*
 * Copyright (c) 1998-2019 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package electionguard.viewer;

import electionguard.ballot.Manifest;
import electionguard.input.ManifestInputValidation;
import electionguard.input.ValidationMessages;
import ucar.ui.prefs.BeanTable;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class ManifestTable extends JPanel {
  private final PreferencesExt prefs;

  private final BeanTable<BallotStyleBean> styleTable;
  private final BeanTable<ContestBean> contestTable;
  private final BeanTable<SelectionBean> selectionTable;
  private final BeanTable<PartyBean> partyTable;
  private final BeanTable<CandidateBean> candidateTable;
  private final BeanTable<GpUnitBean> gpunitTable;

  private final JSplitPane split1, split2, split3, split4, split5;
  private final  TextHistoryPane infoTA = new TextHistoryPane();
  private final IndependentWindow infoWindow;

  private Manifest election;

  public ManifestTable(PreferencesExt prefs) {
    this.prefs = prefs;
    infoWindow = new IndependentWindow("Extra Information", BAMutil.getImage("electionguard-logo.png"), infoTA);
    infoWindow.setBounds((Rectangle) prefs.getBean("InfoWindowBounds", new Rectangle(300, 300, 800, 100)));

    contestTable = new BeanTable<>(ContestBean.class, (PreferencesExt) prefs.node("ContestTable"), false,
            "Contest", "Manifest.ContestDescription", null);
    contestTable.addListSelectionListener(e -> {
      ContestBean contest = contestTable.getSelectedBean();
      if (contest != null) {
        setContest(contest);
      }
    });
    contestTable.addPopupOption("Show Contest", contestTable.makeShowAction(infoTA, infoWindow,
            bean -> ((ContestBean)bean).contest.toString()));

    styleTable = new BeanTable<>(BallotStyleBean.class, (PreferencesExt) prefs.node("StyleTable"), false,
            "BallotStyle", "Manifest.BallotStyle", null);
    styleTable.addPopupOption("Show BallotStyle", styleTable.makeShowAction(infoTA, infoWindow,
            bean -> ((BallotStyleBean)bean).style.toString()));

    selectionTable = new BeanTable<>(SelectionBean.class, (PreferencesExt) prefs.node("SelectionTable"), false,
            "Selection", "Manifest.Selection", null);
    selectionTable.addPopupOption("Show Selection", selectionTable.makeShowAction(infoTA, infoWindow,
            bean -> ((SelectionBean)bean).selection.toString()));

    partyTable = new BeanTable<>(PartyBean.class, (PreferencesExt) prefs.node("PartyTable"), false,
            "Party", "Manifest.Party", null);
    partyTable.addPopupOption("Show Party", partyTable.makeShowAction(infoTA, infoWindow,
            bean -> ((PartyBean)bean).org.toString()));

    candidateTable = new BeanTable<>(CandidateBean.class, (PreferencesExt) prefs.node("CandidateTable"), false,
            "Candidate", "Manifest.Candidate", null);
    candidateTable.addPopupOption("Show Candidate", candidateTable.makeShowAction(infoTA, infoWindow,
            bean -> ((CandidateBean)bean).org.toString()));

    gpunitTable = new BeanTable<>(GpUnitBean.class, (PreferencesExt) prefs.node("GpUnitTable"), false,
            "GeopoliticalUnit", "Manifest.GeopoliticalUnit", null);
    gpunitTable.addPopupOption("Show GeopoliticalUnit", gpunitTable.makeShowAction(infoTA, infoWindow,
            bean -> ((GpUnitBean)bean).gpunit.toString()));

    // layout
    split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, candidateTable, partyTable);
    split1.setDividerLocation(prefs.getInt("splitPos1", 200));

    split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, split1, gpunitTable);
    split2.setDividerLocation(prefs.getInt("splitPos2", 200));

    split3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, split2, styleTable);
    split3.setDividerLocation(prefs.getInt("splitPos3", 200));

    split4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, split3, contestTable);
    split4.setDividerLocation(prefs.getInt("splitPos4", 200));

    split5 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, split4, selectionTable);
    split5.setDividerLocation(prefs.getInt("splitPos5", 200));

    setLayout(new BorderLayout());
    add(split5, BorderLayout.CENTER);
  }

  ManifestTable addActions(JPanel buttPanel) {
    AbstractAction valAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        validateElection();
      }
    };
    BAMutil.setActionProperties(valAction, "alien", "Validate Manifest", false, 'V', -1);
    BAMutil.addActionToContainer(buttPanel, valAction);
    return this;
  }

  void setElectionManifest(Manifest election) {
    this.election = election;
    candidateTable.setBeans(election.getCandidates().stream().map(CandidateBean::new).toList());
    partyTable.setBeans(election.getParties().stream().map(PartyBean::new).toList());
    gpunitTable.setBeans(election.getGeopoliticalUnits().stream().map(GpUnitBean::new).toList());
    styleTable.setBeans(election.getBallotStyles().stream().map(BallotStyleBean::new).toList());
    contestTable.setBeans(election.getContests().stream().map(ContestBean::new).toList());
    selectionTable.setBeans(new ArrayList<>());
  }

  void setContest(ContestBean contestBean) {
    selectionTable.setBeans(
            contestBean.contest.getSelections().stream().map(SelectionBean::new).toList());
  }

  void save() {
    candidateTable.saveState(false);
    partyTable.saveState(false);
    gpunitTable.saveState(false);
    styleTable.saveState(false);
    contestTable.saveState(false);
    selectionTable.saveState(false);
    prefs.putBeanObject("InfoWindowBounds", infoWindow.getBounds());
    prefs.putInt("splitPos1", split1.getDividerLocation());
    prefs.putInt("splitPos2", split2.getDividerLocation());
    prefs.putInt("splitPos3", split3.getDividerLocation());
    prefs.putInt("splitPos4", split4.getDividerLocation());
    prefs.putInt("splitPos5", split5.getDividerLocation());
  }

  void validateElection() {
    if (this.election == null) {
      return;
    }
    ManifestInputValidation input = new ManifestInputValidation(this.election);
    ValidationMessages problems = input.validate();

    infoTA.setText(problems.toString());
    infoTA.appendLine(String.format("Manifest validates %s%n", !problems.hasErrors()));
    infoTA.gotoTop();
    infoWindow.show();
  }

  public static class PartyBean {
    Manifest.Party org;

    public PartyBean(){}

    PartyBean(Manifest.Party org) {
      this.org = org;
    }

    public String getId() {
      return org.getPartyId();
    }
    public String getAbbreviation() {
      return org.getAbbreviation();
    }
    public String getLogoUri() {
      return org.getLogoUri();
    }
    public String getContactInformation() {
      return org.getName().getText().toString();
    }
    public String getColor() {
      return org.getColor();
    }
  }

  public static class CandidateBean {
    Manifest.Candidate org;

    public CandidateBean(){}

    CandidateBean(Manifest.Candidate org) {
      this.org = org;
    }

    public String getId() {
      return org.getCandidateId();
    }
    public String getPartyId() {
      return org.getPartyId();
    }
    public String getImageUri() {
      return org.getImageUri();
    }
    public String getName() {
      return org.getName().getText().toString();
    }
    public boolean isWriteIn() {
      return org.isWriteIn();
    }
  }

  public static class GpUnitBean {
    Manifest.GeopoliticalUnit gpunit;

    public GpUnitBean(){}

    GpUnitBean(Manifest.GeopoliticalUnit gpunit) {
      this.gpunit = gpunit;
    }

    public String getId() {
      return gpunit.getGeopoliticalUnitId();
    }
    public String getType() {
      return gpunit.getType().toString();
    }
    public String getName() {
      return gpunit.getName();
    }
    public String getContactInformation() {
      return gpunit.getContactInformation() == null ? "N/A" : gpunit.getContactInformation().toString();
    }
  }

  public static class BallotStyleBean {
    Manifest.BallotStyle style;

    public BallotStyleBean(){}

    BallotStyleBean(Manifest.BallotStyle style) {
      this.style = style;
    }

    public String getBallotStyle() {
      return style.getBallotStyleId();
    }
    public String getGeopolitical() {
      return style.getGeopoliticalUnitIds().toString();
    }
    public String getParties() {
      return style.getPartyIds().toString();
    }

    public String getImageUrl() {
      return style.getImageUri();
    }
  }

  public static class ContestBean {
    Manifest.ContestDescription contest;

    public ContestBean(){}

    ContestBean(Manifest.ContestDescription contest) {
      this.contest = contest;
    }
    public String getContestId() {
      return contest.getContestId();
    }
    public String getName() {
      return contest.getName();
    }
    public String getElectoralDistrictId() { return contest.getGeopoliticalUnitId(); }

    public String getBallotTitle() {
      if(contest.getBallotTitle() != null) {
        return contest.getBallotTitle().getText().toString();
      } else {
        return "";
      }
    }

    public String getBallotSubTitle() {
      if(contest.getBallotSubtitle() != null) {
        return contest.getBallotSubtitle().getText().toString();
      } else {
        return "";
      }
    }

    public String getCryptoHash() { return contest.getCryptoHash().toString(); }
    public int getNumberElected() { return contest.getNumberElected(); }
    public int getSeq() { return contest.getSequenceOrder(); }
    public String getVoteVariation() { return contest.getVoteVariation().toString(); }
    public int getVotesAllowed() { return contest.getVotesAllowed(); }
  }

  public static class SelectionBean {
    Manifest.SelectionDescription selection;

    public SelectionBean(){}

    SelectionBean(Manifest.SelectionDescription selection) {
      this.selection = selection;
    }

    public String getSelectionId() {
      return selection.getSelectionId();
    }

    public String getCandidateId() {
      return selection.getCandidateId();
    }

    public int getSeq() {
      return selection.getSequenceOrder();
    }

    public String getCryptoHash() { return selection.getCryptoHash().toString(); }
  }

}
