package electionguard.json;

import electionguard.ballot.DecryptingGuardian;
import electionguard.ballot.ElectionConfig;
import electionguard.ballot.ElectionConstants;
import electionguard.ballot.ElectionInitialized;
import electionguard.ballot.EncryptedBallot;
import electionguard.ballot.EncryptedTally;
import electionguard.ballot.Guardian;
import electionguard.ballot.Manifest;
import electionguard.ballot.PlaintextTally;
import electionguard.core.ElementModP;
import electionguard.core.UInt256;
import electionguard.publish.ElectionRecord;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.util.Collections.emptyMap;

public class ElectionRecordJson implements ElectionRecord {
  private static final String PROTO_VERSION = "Json 1.0";
  private final JsonConsumer consumer;

  private ElectionConstants constants;
  private ElectionContextPojo.ElectionContext context;
  private List<DecryptingGuardian> decryptingGuardians;
  private EncryptedTally encryptedTally;
  private List<Guardian> guardians;
  private PlaintextTally tally;

  public ElectionRecordJson(JsonConsumer consumer) {
    this.consumer = consumer;
  }

  @NotNull
  @Override
  public ElectionConstants constants() {
    if (constants == null) {
      constants = consumer.readConstants();
    }
    return constants;
  }

  @Nullable
  @Override
  public UInt256 cryptoBaseHash() {
    if (context == null) {
      context = consumer.readContext();
    }
    return context.cryptoBaseHash;
  }

  @Nullable
  @Override
  public UInt256 cryptoExtendedBaseHash() {
    if (context == null) {
      context = consumer.readContext();
    }
    return context.cryptoExtendedBaseHash;
  }

  @Nullable
  @Override
  public PlaintextTally decryptedTally() {
    if (tally == null) {
      tally = consumer.readDecryptedTally();
    }
    return tally;
  }

  @NotNull
  @Override
  public List<DecryptingGuardian> decryptingGuardians() {
    if (decryptingGuardians == null) {
      decryptingGuardians = consumer.readDecryptingGuardians();
    }
    return decryptingGuardians;
  }

  @Nullable
  @Override
  public ElectionInitialized electionInit() {
    if (context == null) {
      context = consumer.readContext();
    }
    ElectionConfig config = new ElectionConfig(
            PROTO_VERSION,
            constants(),
            manifest(),
            numberOfGuardians(),
            quorum(),
            emptyMap()
    );
    return new ElectionInitialized(
            config,
            jointPublicKey(),
            context.manifestHash,
            cryptoBaseHash(),
            cryptoExtendedBaseHash(),
            guardians(),
            emptyMap()
    );
  }

  @NotNull
  @Override
  public Iterable<EncryptedBallot> encryptedBallots(@Nullable Function1<? super EncryptedBallot, Boolean> function1) {
    return consumer.iteratorEncryptedBallots();
  }

  @Nullable
  @Override
  public EncryptedTally encryptedTally() {
    if (encryptedTally == null) {
      encryptedTally = consumer.readEncryptedTally();
    }
    return encryptedTally;
  }

  @NotNull
  @Override
  public List<Guardian> guardians() {
    if (guardians == null) {
      guardians = consumer.readGuardians();
    }
    return guardians;
  }

  @Nullable
  @Override
  public ElementModP jointPublicKey() {
    if (context == null) {
      context = consumer.readContext();
    }
    return context.publicKey;
  }

  @NotNull
  @Override
  public Manifest manifest() {
    return consumer.readManifest();
  }

  @Override
  public int numberOfGuardians() {
    if (context == null) {
      context = consumer.readContext();
    }
    return context.numberOfGuardians;
  }

  @NotNull
  @Override
  public String protoVersion() {
    return PROTO_VERSION;
  }

  @Override
  public int quorum() {
    if (context == null) {
      context = consumer.readContext();
    }
    return context.quorum;
  }

  @NotNull
  @Override
  public Iterable<PlaintextTally> spoiledBallotTallies() {
    return consumer.iteratorSpoiledBallotTallies();
  }

  @NotNull
  @Override
  public Stage stage() {
    return consumer.stage();
  }

  @NotNull
  @Override
  public String topdir() {
    return consumer.location();
  }
}
