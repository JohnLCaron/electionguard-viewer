package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/** Conversion between Encrypt.EncryptionDevice and Json, using python's object model. */
public class EncryptionDevicePojo {
  /** Unique identifier for device. */
  public Long device_id;
  /** Used to identify session and protect the timestamp. */
  public Long session_id;
  /** Election initialization value. */
  public Long launch_code;
  /** Arbitrary string to designate the location of the device. */
  public String location;

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static EncryptionDevice deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    return new EncryptionDevice(gson.fromJson(jsonElem, EncryptionDevicePojo.class));
  }

  public static class EncryptionDevice {
    /** Unique identifier for device. */
    public final Long deviceId;
    /** Used to identify session and protect the timestamp. */
    public final Long sessionId;
    /** Election initialization value. */
    public final Long launchCode;
    /** Arbitrary string to designate the location of the device. */
    public final String location;

    EncryptionDevice(EncryptionDevicePojo pojo) {
      this.deviceId = pojo.device_id;
      this.sessionId = pojo.session_id;
      this.launchCode = pojo.launch_code;
      this.location = pojo.location;
    }

  }


}
