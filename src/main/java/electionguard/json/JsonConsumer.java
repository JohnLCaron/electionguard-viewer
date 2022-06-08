package electionguard.json;

import com.google.common.flogger.FluentLogger;
import com.google.gson.Gson;
import electionguard.ballot.DecryptingGuardian;
import electionguard.ballot.ElectionConstants;
import electionguard.ballot.EncryptedBallot;
import electionguard.ballot.EncryptedTally;
import electionguard.ballot.Guardian;
import electionguard.ballot.Manifest;
import electionguard.ballot.PlaintextTally;
import electionguard.core.ElementModQ;
import electionguard.publish.ElectionRecord;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Reads published election records in JSON. */
public class JsonConsumer {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final Gson enhancedGson = GsonTypeAdapters.enhancedGson();

  private final JsonElectionRecordPath paths;

  public JsonConsumer(String topDir) throws IOException {
    paths = new JsonElectionRecordPath(Path.of(topDir));
  }

  public boolean isValidElectionRecord(Formatter error) {
    if (!Files.exists(paths.topPath())) {
      error.format("%s does not exist", paths.topPath());
      return false;
    }
    if (!Files.exists(paths.manifestPath())) {
      error.format("%s does not exist", paths.manifestPath());
      return false;
    }
    return true;
  }

  public String location() {
    return paths.topPath().toAbsolutePath().toString();
  }

  public ElectionRecord.Stage stage() {
    if (Files.exists(paths.tallyPath())) {
      return ElectionRecord.Stage.DECRYPTED;
    }
    if (Files.exists(paths.encryptedTallyPath())) {
      return ElectionRecord.Stage.TALLIED;
    }
    if (paths.ballotFiles().length > 0) {
      return ElectionRecord.Stage.ENCRYPTED;
    }
    if (Files.exists(paths.contextPath())) {
      return ElectionRecord.Stage.INIT;
    }
    return ElectionRecord.Stage.CONFIG;
  }

  public ElectionRecord readElectionRecord() throws IOException {
    Formatter error = new Formatter();
    if (!isValidElectionRecord(error)) {
      throw new FileNotFoundException(String.format("Invalid path: %s", error));
    }
    return new ElectionRecordJson(this);
  }

  @Nullable
  public Manifest readManifest() {
    if (Files.exists(paths.manifestPath())) {
      try (InputStream is = new FileInputStream(paths.manifestPath().toString())) {
        Reader reader = new InputStreamReader(is);
        return enhancedGson.fromJson(reader, Manifest.class);
      } catch (Exception e) {
        logger.atSevere().withCause(e).log("Failed readElection file '%s'", paths.manifestPath().toString());
        return null;
      }
    }
    return null;
  }

  @Nullable
  public ElectionConstants readConstants() {
    if (Files.exists(paths.constantsPath())) {
      try (InputStream is = new FileInputStream(paths.constantsPath().toString())) {
        Reader reader = new InputStreamReader(is);
        return enhancedGson.fromJson(reader, ElectionConstants.class);
      } catch (Exception e) {
        logger.atSevere().withCause(e).log("Failed readConstants file '%s'", paths.constantsPath().toString());
        return null;
      }
    }
    return null;
  }

  @Nullable
  public ElectionContextPojo.ElectionContext readContext() {
    if (Files.exists(paths.contextPath())) {
      try (InputStream is = new FileInputStream(paths.contextPath().toString())) {
        Reader reader = new InputStreamReader(is);
        return enhancedGson.fromJson(reader, ElectionContextPojo.ElectionContext.class);
      } catch (Exception e) {
        logger.atSevere().withCause(e).log("Failed readContext file '%s'", paths.contextPath().toString());
        return null;
      }
    }
    return null;
  }

  @Nullable
  public PlaintextTally readDecryptedTally() {
    if (Files.exists(paths.tallyPath())) {
      try (InputStream is = new FileInputStream(paths.tallyPath().toString())) {
        Reader reader = new InputStreamReader(is);
        return enhancedGson.fromJson(reader, PlaintextTally.class);
      } catch (Exception e) {
        logger.atSevere().withCause(e).log("Failed readDecryptedTally file '%s'", paths.tallyPath().toString());
        return null;
      }
    }
    return null;
  }

  @Nullable
  public EncryptedTally readEncryptedTally() {
    if (Files.exists(paths.encryptedTallyPath())) {
      try (InputStream is = new FileInputStream(paths.encryptedTallyPath().toString())) {
        Reader reader = new InputStreamReader(is);
        return enhancedGson.fromJson(reader, EncryptedTally.class);
      } catch (Exception e) {
        logger.atSevere().withCause(e).log("Failed readDecryptedTally file '%s'", paths.encryptedTallyPath().toString());
        return null;
      }
    }
    return null;
  }

  public List<EncryptionDevicePojo.EncryptionDevice> readDevices() {
    List<EncryptionDevicePojo.EncryptionDevice> result = new ArrayList<>();
    for (File file : paths.deviceFiles()) {
      EncryptionDevicePojo.EncryptionDevice fromPython = readDevice(file.getAbsolutePath());
      if (fromPython != null) {
        result.add(fromPython);
      }
    }
    return result;
  }

  public EncryptionDevicePojo.EncryptionDevice readDevice(String pathname) {
    try (InputStream is = new FileInputStream(pathname)) {
      Reader reader = new InputStreamReader(is);
      return enhancedGson.fromJson(reader, EncryptionDevicePojo.EncryptionDevice.class);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Failed readDevice file '%s'", pathname);
      return null;
    }
  }

  public Iterable<EncryptedBallot> iteratorEncryptedBallots() {
    List<EncryptedBallot> result = new ArrayList<>();
    for (File file : paths.ballotFiles()) {
      EncryptedBallot fromPython = readEncryptedBallot(file.getAbsolutePath());
      if (fromPython != null) {
        result.add(fromPython);
      }
    }
    return result;
  }

  public static EncryptedBallot readEncryptedBallot(String pathname) {
    try (InputStream is = new FileInputStream(pathname)) {
      Reader reader = new InputStreamReader(is);
      return enhancedGson.fromJson(reader, EncryptedBallot.class);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Failed readSubmittedBallot file '%s'", pathname);
      return null;
    }
  }

  public List<DecryptingGuardian> readDecryptingGuardians() {
    if (!paths.coefficientsPath().toFile().exists()) {
      return new ArrayList<>();
    }
    // make a map of the Guardians
    Map<String, Guardian> grMap = readGuardians().stream().collect(
            Collectors.toMap(Guardian::getGuardianId, gr -> gr));

    // read in the coefficients file
    LagrangeCoefficientsPojo coeffPojo = null;
    try (InputStream is = new FileInputStream(paths.coefficientsPath().toString())) {
      Reader reader = new InputStreamReader(is);
      coeffPojo = enhancedGson.fromJson(reader, LagrangeCoefficientsPojo.class);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Failed readCoefficients file '%s'", paths.coefficientsPath().toString());
      return new ArrayList<>();
    }

    // construct the DecryptingGuardians
    List<DecryptingGuardian> result = new ArrayList<>();
    for (Map.Entry<String, ElementModQ> entry : coeffPojo.coefficients.entrySet()) {
      Guardian gr = grMap.get(entry.getKey());
      DecryptingGuardian avail = new DecryptingGuardian(entry.getKey(), gr.getXCoordinate(), entry.getValue());
      result.add(avail);
    }
    return result;
  }

  // Decrypted, spoiled ballots
  public Iterable<PlaintextTally> iteratorSpoiledBallotTallies() {
    List<PlaintextTally> result = new ArrayList<>();
    for (File file : paths.spoiledBallotFiles()) {
        PlaintextTally fromPython = readPlaintextTally(file.getAbsolutePath());
        if (fromPython != null) {
          result.add(fromPython);
        }
    }
    return result;
  }

  public static PlaintextTally readPlaintextTally(String pathname) {
    try (InputStream is = new FileInputStream(pathname)) {
      Reader reader = new InputStreamReader(is);
      return enhancedGson.fromJson(reader, PlaintextTally.class);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Failed readPlaintextTally file '%s'", pathname);
      return null;
    }
  }

  public List<Guardian> readGuardians() {
    List<Guardian> result = new ArrayList<>();
    for (File file : paths.guardianFiles()) {
      Guardian guardian = readGuardian(file.getAbsolutePath());
      if (guardian != null) {
        result.add(guardian);
      }
    }
    return result;
  }

  public static Guardian readGuardian(String pathname) {
    try (InputStream is = new FileInputStream(pathname)) {
      Reader reader = new InputStreamReader(is);
      return enhancedGson.fromJson(reader, Guardian.class);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Failed readGuardian file '%s'", pathname);
    }
    return null;
  }
}
