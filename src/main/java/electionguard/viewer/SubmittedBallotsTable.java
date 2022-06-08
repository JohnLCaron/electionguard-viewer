/*
 * Copyright (c) 1998-2019 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package electionguard.viewer;

import electionguard.ballot.ElectionInitialized;
import electionguard.ballot.Manifest;
import electionguard.ballot.EncryptedBallot;
import electionguard.publish.ElectionRecord;
import ucar.ui.prefs.BeanTable;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

public class SubmittedBallotsTable extends JPanel {
  private final PreferencesExt prefs;

  private final BeanTable<SubmittedBallotBean> ballotTable;
  private final BeanTable<ContestBean> contestTable;
  private final BeanTable<SelectionBean> selectionTable;

  private final JSplitPane split1, split2;
  private final IndependentWindow infoWindow;

  private Manifest manifest;
  private ElectionRecord context;

  public SubmittedBallotsTable(PreferencesExt prefs) {
    this.prefs = prefs;
    TextHistoryPane infoTA = new TextHistoryPane();
    infoWindow = new IndependentWindow("Extra Information", BAMutil.getImage("electionguard-logo.png"), infoTA);
    infoWindow.setBounds((Rectangle) prefs.getBean("InfoWindowBounds", new Rectangle(300, 300, 800, 100)));

    ballotTable = new BeanTable<>(SubmittedBallotBean.class, (PreferencesExt) prefs.node("BallotTable"), false,
            "SubmittedBallot", "encrypted_ballots", null);
    ballotTable.addListSelectionListener(e -> {
      SubmittedBallotBean ballot = ballotTable.getSelectedBean();
      if (ballot != null) {
        setBallot(ballot);
      }
    });
    ballotTable.addPopupOption("Show Ballot", ballotTable.makeShowAction(infoTA, infoWindow,
            bean -> ((SubmittedBallotBean)bean).ballot.toString()));
    // ballotTable.addPopupOption("Compute Ballot Size", ballotTable.makeShowAction(infoTA, infoWindow,
    //        bean -> computeBallotSize(((SubmittedBallotBean)bean).ballot)));

    contestTable = new BeanTable<>(ContestBean.class, (PreferencesExt) prefs.node("ContestTable"), false,
            "Contest", "CiphertextBallot.Contest", null);
    contestTable.addListSelectionListener(e -> {
      ContestBean contest = contestTable.getSelectedBean();
      if (contest != null) {
        setContest(contest);
      }
    });
    contestTable.addPopupOption("Show Contest", contestTable.makeShowAction(infoTA, infoWindow,
            bean -> bean.toString()));

    selectionTable = new BeanTable<>(SelectionBean.class, (PreferencesExt) prefs.node("SelectionTable"), false,
            "Selection", "CiphertextBallot.Selection", null);
    selectionTable.addPopupOption("Show Selection", selectionTable.makeShowAction(infoTA, infoWindow,
            bean -> bean.toString()));

    // layout
    split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, ballotTable, contestTable);
    split1.setDividerLocation(prefs.getInt("splitPos1", 200));

    split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, split1, selectionTable);
    split2.setDividerLocation(prefs.getInt("splitPos2", 200));

    setLayout(new BorderLayout());
    add(split2, BorderLayout.CENTER);
  }

  void setAcceptedBallots(ElectionRecord record, Iterable<EncryptedBallot> acceptedBallots) {
    this.manifest = record.manifest();
    this.context = context;
    java.util.List<SubmittedBallotBean> beanList = new ArrayList<>();
    for (EncryptedBallot ballot : acceptedBallots) {
      beanList.add(new SubmittedBallotBean(ballot));
    }
      ballotTable.setBeans(beanList);
      if (beanList.size() > 0) {
        setBallot(beanList.get(0));
      } else {
        contestTable.setBeans(new ArrayList<>());
        selectionTable.setBeans(new ArrayList<>());
      }
  }

  void setBallot(SubmittedBallotBean ballotBean) {
    java.util.List<ContestBean> beanList = new ArrayList<>();
    for (EncryptedBallot.Contest c : ballotBean.ballot.getContests()) {
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
    java.util.List<SelectionBean> beanList = new ArrayList<>();
    for (EncryptedBallot.Selection s : contestBean.contest.getSelections()) {
      beanList.add(new SelectionBean(s, contestBean));
    }
    selectionTable.setBeans(beanList);
  }

  void save() {
    ballotTable.saveState(false);
    contestTable.saveState(false);
    selectionTable.saveState(false);
    prefs.putBeanObject("InfoWindowBounds", infoWindow.getBounds());
    prefs.putInt("splitPos1", split1.getDividerLocation());
    prefs.putInt("splitPos2", split2.getDividerLocation());
  }

  /*
  String computeBallotSize(EncryptedBallot ballot) {
    int intSizes = 0;
    int stringSizes = 0;
    int countContests = 0;
    int countSelections = 0;
    int countCipher = 0;
    int countConstantProof = 0;
    int countDisjunctProof = 0;
    int countQ = 0;

    countQ += 4;
    intSizes += 2;
    stringSizes += ballot.ballotId.length();
    stringSizes += ballot.ballotStyleId.length();

    for (CiphertextBallot.Contest contest : ballot.contests) {
      countContests++;
      stringSizes += contest.contestId.length();
      intSizes++;
      countQ += 2;
      countCipher++;
      if (contest.nonce.isPresent()) {
        countQ++;
      }
      if (contest.proof.isPresent()) {
        countConstantProof++;
      }

      for (CiphertextBallot.Selection selection : contest.selections) {
        countSelections++;
        stringSizes += selection.selectionId.length();
        intSizes++;
        countQ += 2;
        countCipher++;
        if (selection.nonce.isPresent()) {
          countQ++;
        }
        if (selection.proof.isPresent()) {
          countDisjunctProof++;
        }
        if (selection.extended_data.isPresent()) {
          countCipher++;
        }
      }
    }

    int sizeQ = 32;
    int sizeP = 512;
    int constantProofSize = 2 * sizeQ + 2 * sizeP;
    int disjunctProofSize = 5 * sizeQ + 4 * sizeP;
    int cipherSize = 2 * sizeP;

    Formatter f = new Formatter();
    f.format(" string sizes    = %d, intSize = %d%n", stringSizes, intSizes * 4);
    int count = stringSizes + intSizes * 4;
    f.format(" countContests   = %3d, constantProofSize (2Q, 2P)=%4d, total = %6d%n",
              countConstantProof, constantProofSize, countConstantProof * constantProofSize);
    count += countConstantProof * constantProofSize;
    f.format(" countSelections = %3d, disjunctProofSize (5Q, 4P)=%4d, total = %6d%n",
              countDisjunctProof, disjunctProofSize, countDisjunctProof * disjunctProofSize);
    count += countDisjunctProof * disjunctProofSize;
    f.format(" countCipher     = %3d, cipherSize (2P)           =%4d  total = %6d%n",
            countCipher, cipherSize, countCipher * cipherSize);
    count += countCipher * cipherSize;
    f.format(" countQ          = %3d, sizeQ                     =%4d, total = %6d%n",
            countQ, sizeQ, countQ * sizeQ);
    count += countQ * sizeQ;
    f.format("%ntotal= %d%n", count);

    return f.toString();
  } */

  public static class SubmittedBallotBean {
    EncryptedBallot ballot;

    public SubmittedBallotBean(){}

    SubmittedBallotBean(EncryptedBallot ballot) {
      this.ballot = ballot;
    }

    public String getId() {
      return ballot.getBallotId();
    }

    public String getCode() {
      return ballot.getCode().toString();
    }

    public String getCodeSeed() {
      return ballot.getCodeSeed().toString();
    }

    public String getState() {
      return ballot.getState().toString();
    }

    public String getStyle() {
      return ballot.getBallotStyleId();
    }

    public String getTimeStamp() {
      return OffsetDateTime.ofInstant(Instant.ofEpochSecond(ballot.getTimestamp()), ZoneId.of("UTC")).toString();
    }
  }

  public class ContestBean {
    EncryptedBallot.Contest contest;
    Manifest.ContestDescription mcontest;

    public ContestBean(){}

    ContestBean(EncryptedBallot.Contest contest) {
      this.contest = contest;
      this.mcontest = manifest.getContests().stream().filter(it -> it.getContestId().equals(contest.getContestId())).findFirst().orElseThrow();
    }

    public String getContestId() {
      return contest.getContestId();
    }

    public boolean isProof() {
      return contest.getProof() != null;
    }

    public String getName() {
      return mcontest.getName();
    }

    public String getVoteVariation() {
      return mcontest.getVoteVariation().toString();
    }

    public Integer getVotesAllowed() {
      return mcontest.getVotesAllowed();
    }

    @Override
    public String toString() {
      return String.format("CiphertextBallot.Contest%n %s%n%nManifest.ContestDescription%n%s%n",
              contest , mcontest.toString());
    }
  }

  public class SelectionBean {
    EncryptedBallot.Selection selection;
    Manifest.SelectionDescription mselection;

    public SelectionBean(){}

    SelectionBean(EncryptedBallot.Selection selection, ContestBean contestBean) {
      this.selection = selection;
      Optional<Manifest.SelectionDescription> match = contestBean.mcontest.getSelections().stream().filter(it -> it.getSelectionId().equals(selection.getSelectionId())).findFirst();
      if (match.isPresent()) {
        this.mselection = match.get();
      } else {
        java.util.List<String> list = contestBean.mcontest.getSelections().stream().map(it -> it.getSelectionId()).toList();
        // System.out.printf("Looking for %s in %s%n", selection.getSelectionId(), list);
      }
    }

    public String getSelectionId() {
      return selection.getSelectionId();
    }

    public String getCandidateId() {
      return mselection != null ? mselection.getCandidateId() : "N/A";
    }

    public String getCryptoHash() {
      return selection.getCryptoHash().toString();
    }

    public boolean isPlaceholderSelection() {
      return selection.isPlaceholderSelection();
    }

    // String where, Group.ElementModQ selectionHash, Group.ElementModP publicKey, Group.ElementModQ cryptoExtendedBaseHash
    //public boolean isValid() {
    //  return selection.getProof().("test", selection.selectionHash, context.electionPublicKey(), context.extendedHash());
   // }

    @Override
    public String toString() {
      return String.format("CiphertextBallot.Selection%n %s%n%nManifest.SelectionDescription%n%s%n",
              selection , mselection != null ? mselection.toString() : "N/A");
    }
  }

}
