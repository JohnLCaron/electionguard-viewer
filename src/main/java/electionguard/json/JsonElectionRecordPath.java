package electionguard.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonElectionRecordPath {
  static final String JSON_SUFFIX = ".json";

  static final String DEVICES_DIR = "encryption_devices";
  static final String GUARDIANS_DIR = "guardians";
  static final String SUBMITTED_BALLOTS_DIR = "submitted_ballots"; // encrypted
  static final String SPOILED_BALLOTS_DIR = "spoiled_ballots"; // plaintext
  static final String INVALID_BALLOTS_DIR = "invalid_ballots"; // plaintext

  static final String MANIFEST_FILE_NAME = "manifest" + JSON_SUFFIX;
  static final String CONTEXT_FILE_NAME = "context" + JSON_SUFFIX;
  static final String CONSTANTS_FILE_NAME = "constants" + JSON_SUFFIX;
  static final String COEFFICIENTS_FILE_NAME = "coefficients" + JSON_SUFFIX;
  static final String ENCRYPTED_TALLY_FILE_NAME = "encrypted_tally" + JSON_SUFFIX;
  static final String TALLY_FILE_NAME = "tally" + JSON_SUFFIX;

  static final String DEVICE_PREFIX = "device_";
  static final String GUARDIAN_PREFIX = "guardian_";
  static final String SPOILED_BALLOT_PREFIX = "spoiled_ballot_";
  static final String SUBMITTED_BALLOT_PREFIX = "submitted_ballot_";
  static final String PLAINTEXT_BALLOT_PREFIX = "plaintext_ballot_";

  static final String AVAILABLE_GUARDIAN_PREFIX = "available_guardian_";

  final String topdir;
  final Path electionRecordDir;
  final Path devicesDirPath;
  final Path ballotsDirPath;
  final Path spoiledBallotDirPath;
  final Path guardianDirPath;

  public JsonElectionRecordPath(Path electionRecordDir) throws IOException {
    this.topdir = electionRecordDir.toAbsolutePath().toString();

    this.electionRecordDir = electionRecordDir;
    this.devicesDirPath = electionRecordDir.resolve(DEVICES_DIR);
    this.ballotsDirPath = electionRecordDir.resolve(SUBMITTED_BALLOTS_DIR);
    this.guardianDirPath = electionRecordDir.resolve(GUARDIANS_DIR);
    this.spoiledBallotDirPath = electionRecordDir.resolve(SPOILED_BALLOTS_DIR);
    // this.availableGuardianDirPath = publishDirectory.resolve(AVAILABLE_GUARDIANS_DIR);
  }

  public Path manifestPath() {
    return electionRecordDir.resolve(MANIFEST_FILE_NAME).toAbsolutePath();
  }

  public Path contextPath() {
    return electionRecordDir.resolve(CONTEXT_FILE_NAME).toAbsolutePath();
  }

  public Path constantsPath() {
    return electionRecordDir.resolve(CONSTANTS_FILE_NAME).toAbsolutePath();
  }

  public Path coefficientsPath() {
    return electionRecordDir.resolve(COEFFICIENTS_FILE_NAME).toAbsolutePath();
  }

  public Path topPath() {
    return electionRecordDir.toAbsolutePath();
  }

  public Path tallyPath() {
    return electionRecordDir.resolve(TALLY_FILE_NAME).toAbsolutePath();
  }

  public Path encryptedTallyPath() {
    return electionRecordDir.resolve(ENCRYPTED_TALLY_FILE_NAME).toAbsolutePath();
  }

  public Path devicePath(String id) {
    return devicesDirPath.resolve(DEVICE_PREFIX + id + JSON_SUFFIX);
  }

  public File[] deviceFiles() {
    if (!Files.exists(devicesDirPath) || !Files.isDirectory(devicesDirPath)) {
      return new File[0];
    }
    return devicesDirPath.toFile().listFiles();
  }

  public Path guardianRecordsPath(String id) {
    String fileName = GUARDIAN_PREFIX + id + JSON_SUFFIX;
    return guardianDirPath.resolve(fileName);
  }

  public File[] guardianFiles() {
    if (!Files.exists(guardianDirPath) || !Files.isDirectory(guardianDirPath)) {
      return new File[0];
    }
    return guardianDirPath.toFile().listFiles();
  }

  public Path ballotPath(String id) {
    String fileName = SUBMITTED_BALLOT_PREFIX + id + JSON_SUFFIX;
    return ballotsDirPath.resolve(fileName);
  }

  public File[] ballotFiles() {
    if (!Files.exists(ballotsDirPath) || !Files.isDirectory(ballotsDirPath)) {
      return new File[0];
    }
    return ballotsDirPath.toFile().listFiles();
  }

  public Path spoiledBallotPath(String id) {
    String fileName = SPOILED_BALLOT_PREFIX + id + JSON_SUFFIX;
    return spoiledBallotDirPath.resolve(fileName);
  }

  public File[] spoiledBallotFiles() {
    if (!Files.exists(spoiledBallotDirPath) || !Files.isDirectory(spoiledBallotDirPath)) {
      return new File[0];
    }
    return spoiledBallotDirPath.toFile().listFiles();
  }

}
