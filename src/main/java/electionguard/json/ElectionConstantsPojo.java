package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import electionguard.ballot.ElectionConstants;

import java.math.BigInteger;

/** Conversion between ElectionConstants and Json, using python's object model. */
public class ElectionConstantsPojo {
  public String name;
  public BigInteger large_prime;
  public BigInteger small_prime;
  public BigInteger cofactor;
  public BigInteger generator;

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static ElectionConstants deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    ElectionConstantsPojo pojo = gson.fromJson(jsonElem, ElectionConstantsPojo.class);
    return new ElectionConstants(
            "from Json",
            pojo.large_prime.toByteArray(),
            pojo.small_prime.toByteArray(),
            pojo.cofactor.toByteArray(),
            pojo.generator.toByteArray()
            );
  }

}
