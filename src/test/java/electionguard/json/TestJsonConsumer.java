package electionguard.json;

import electionguard.ballot.DecryptingGuardian;
import electionguard.ballot.ElectionConstants;
import electionguard.ballot.EncryptedBallot;
import electionguard.ballot.EncryptedTally;
import electionguard.ballot.Guardian;
import electionguard.ballot.PlaintextTally;
import electionguard.core.ElementModP;
import electionguard.publish.ElectionRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Formatter;

import static com.google.common.truth.Truth.assertThat;
import static electionguard.viewer.KUtils.productionGroup;

public class TestJsonConsumer {
  String topdir = "/home/snake/tmp/electionguard/electionRecord25May2022";

  @Test
  public void testJsonManifest() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    assertThat(consumer.isValidElectionRecord(new Formatter())).isTrue();
    ElectionRecord record = consumer.readElectionRecord();
    assertThat(record).isNotNull();
    assertThat(record.manifest()).isNotNull();
    assertThat(record.topdir()).isEqualTo(topdir);
  }

  @Test
  public void testJsonConstants() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    assertThat(consumer.readConstants()).isNotNull();
    ElectionConstants constants = consumer.readConstants();
    ElectionConstants expected = productionGroup().getConstants();

    assertThat(constants.getLargePrime()).isEqualTo(expected.getLargePrime());
    assertThat(constants.getSmallPrime()).isEqualTo(expected.getSmallPrime());
    assertThat(constants.getCofactor()).isEqualTo(expected.getCofactor());
    assertThat(constants.getGenerator()).isEqualTo(expected.getGenerator());

    ElementModP etest = productionGroup().safeBinaryToElementModP(expected.getLargePrime(), 0);
    ElementModP test = productionGroup().safeBinaryToElementModP(constants.getLargePrime(), 0);
    assertThat(test).isEqualTo(etest);
  }

  @Test
  public void testJsonOther() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    assertThat(consumer.readContext()).isNotNull();
    assertThat(consumer.readDevices()).isNotEmpty();
  }

  @Test
  public void testJsonGuardians() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    for (Guardian guardian : consumer.readGuardians()) {
      assertThat(guardian).isNotNull();
      System.out.printf("guardian id %s%n", guardian.getGuardianId());
    }
  }

  @Test
  public void testJsonEncryptedBallots() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    for (EncryptedBallot ballot : consumer.iteratorEncryptedBallots()) {
      assertThat(ballot).isNotNull();
    }
  }

  @Test
  public void testJsonEncryptedTally() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    EncryptedTally etally = consumer.readEncryptedTally();
    assertThat(etally).isNotNull();
  }

  @Test
  public void testJsonDecryptedTally() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    PlaintextTally dtally = consumer.readDecryptedTally();
    assertThat(dtally).isNotNull();
  }

  @Test
  public void testJsonDecryptingGuardian() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    for (DecryptingGuardian guardian : consumer.readDecryptingGuardians()) {
      assertThat(guardian).isNotNull();
    }
  }

  @Test
  public void testJsonEncryptedSpoiledBallotTallies() throws IOException {
    JsonConsumer consumer = new JsonConsumer(topdir);
    for (PlaintextTally tally : consumer.iteratorSpoiledBallotTallies()) {
      assertThat(tally).isNotNull();
    }
  }
}
