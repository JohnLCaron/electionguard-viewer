/*
 * Copyright (c) 1998-2019 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package electionguard.viewer;

import electionguard.core.ElementModP;
import electionguard.decrypt.DecryptingTrustee;
import electionguard.decrypt.DecryptingTrusteeIF;
import electionguard.keyceremony.SecretKeyShare;
import ucar.ui.prefs.BeanTable;
import ucar.ui.widget.BAMutil;
import ucar.ui.widget.IndependentWindow;
import ucar.ui.widget.TextHistoryPane;
import ucar.util.prefs.PreferencesExt;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Map;

public class DecryptingTrusteeTable extends JPanel {
  private final PreferencesExt prefs;

  private final BeanTable<TrusteeBean> trusteeTable;
  private final BeanTable<PartialKeyBackupBean> backupTable;
  private final BeanTable<GuardianCommittmentsBean> commitmentTable;

  private final JSplitPane split1;
  private final JSplitPane split2;

  private final TextHistoryPane infoTA;
  private final IndependentWindow infoWindow;

  private DecryptingTrusteeIF current;

  public DecryptingTrusteeTable(PreferencesExt prefs) {
    this.prefs = prefs;
    infoTA = new TextHistoryPane();
    infoWindow = new IndependentWindow("Extra Information", BAMutil.getImage("electionguard-logo.png"), infoTA);
    infoWindow.setBounds((Rectangle) prefs.getBean("InfoWindowBounds", new Rectangle(300, 300, 800, 100)));

    trusteeTable = new BeanTable<>(TrusteeBean.class, (PreferencesExt) prefs.node("ContestTable"), false);
    /* trusteeTable.addListSelectionListener(e -> {
      TrusteeBean bean = trusteeTable.getSelectedBean();
      if (bean != null) {
        setTrustee(bean);
      }
    }); */

    backupTable = new BeanTable<>(PartialKeyBackupBean.class, (PreferencesExt) prefs.node("backupTable"), false);
    commitmentTable = new BeanTable<>(GuardianCommittmentsBean.class, (PreferencesExt) prefs.node("commTable"), false);

    // layout
    split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, backupTable, commitmentTable);
    split2.setDividerLocation(prefs.getInt("splitPos2", 200));

    split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, trusteeTable, split2);
    split1.setDividerLocation(prefs.getInt("splitPos1", 200));

    setLayout(new BorderLayout());
    add(split1, BorderLayout.CENTER);
  }

  void clearBeans() {
    trusteeTable.clearBeans();
    backupTable.clearBeans();
    commitmentTable.clearBeans();
  }

  void setTrustees(Iterable<DecryptingTrusteeIF> trustees) {
    this.current = null;
    java.util.List<TrusteeBean> beanList = new ArrayList<>();
    for (DecryptingTrusteeIF trustee : trustees)  {
      if (this.current == null) {
        this.current = trustee;
      }
      beanList.add(new TrusteeBean(trustee));
    }
    trusteeTable.setBeans(beanList);
  }

  /*
  void setTrusteePrivateData(TrusteeBean trusteeBean) {
    this.current = trusteeBean.object;

    java.util.List<PartialKeyBackupBean> beanList = new ArrayList<>();
    for (electionguard.keyceremony.SecretKeyShare e : trusteeBean.object.getSecretKeyShares().values()) {
      beanList.add(new PartialKeyBackupBean(e.getGeneratingGuardianId(), e));
    }
    backupTable.setBeans(beanList);

    java.util.List<GuardianCommittmentsBean> bean2List = new ArrayList<>();
    for (Map.Entry<String, java.util.List<ElementModP>> e : trusteeBean.object.getCoefficientCommitments().entrySet()) {
      bean2List.add(new GuardianCommittmentsBean(e.getKey(), e.getValue()));
    }
    commitmentTable.setBeans(bean2List);
  } */

  void save() {
    backupTable.saveState(false);
    trusteeTable.saveState(false);
    commitmentTable.saveState(false);
    prefs.putBeanObject("InfoWindowBounds", infoWindow.getBounds());
    prefs.putInt("splitPos1", split1.getDividerLocation());
    prefs.putInt("splitPos2", split2.getDividerLocation());
  }

  void showInfo(Formatter f) {
    if (this.current != null) {
      f.format("%s%n", this.current);
    }
  }

  public class TrusteeBean {
    DecryptingTrusteeIF object;
    public TrusteeBean(){}
    TrusteeBean(DecryptingTrusteeIF object) {
      this.object = object;
    }

    public String getId() {
      return object.id();
    }
    public int getXCoordinate() {return object.xCoordinate();} // UInt not visible ??
    public String getElectionPublicKey() {
      return object.electionPublicKey().toString();
    }
  }

  public class PartialKeyBackupBean {
    String key;
    SecretKeyShare object;

    public PartialKeyBackupBean(){}

    PartialKeyBackupBean(String key, SecretKeyShare object) {
      this.key = key;
      this.object = object;
    }

    public String getKey() {
      return key;
    }
    public String getGeneratingGuardianId() {
      return object.getGeneratingGuardianId();
    }
    public String getDesignatedGuardianId() {
      return object.getDesignatedGuardianId();
    }
    public String getCoordinate() {
      return object.getEncryptedCoordinate().toString();
    }
  }

  public class GuardianCommittmentsBean {
    String key;
    java.util.List<ElementModP> object;

    public GuardianCommittmentsBean(){}

    GuardianCommittmentsBean(String key, java.util.List<ElementModP> object) {
      this.key = key;
      this.object = object;
    }

    public String getKey() {
      return key;
    }

    public String getCommitments() {
      Formatter f = new Formatter();
      for (ElementModP modp :  object) {
        f.format("%s, ", modp.toStringShort());
      }
      return f.toString();
    }
  }

}
