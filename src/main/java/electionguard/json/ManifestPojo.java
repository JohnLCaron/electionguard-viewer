package electionguard.json;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import electionguard.ballot.Manifest;
import electionguard.core.UInt256;

import javax.annotation.Nullable;
import java.util.List;

import static electionguard.ballot.ManifestKt.contestDescriptionCryptoHash;
import static electionguard.ballot.ManifestKt.manifestCryptoHash;
import static electionguard.ballot.ManifestKt.selectionDescriptionCryptoHash;
import static electionguard.core.HashKt.hashElements;
import static java.util.Collections.emptyList;

/**
 * Helper class for conversion of Manifest description to/from Json, using python's object model.
 * LOOK: set the crypto as null everywhere?
 */
public class ManifestPojo {
  public String election_scope_id;
  public String spec_version;
  public String type;
  public String start_date; // LOOK specify ISO-8601 format
  public String end_date; // ISO-8601 Local or UTC? Assume local has zone offset
  public InternationalizedText name;
  public ContactInformation contact_information;

  public List<GeopoliticalUnit> geopolitical_units;
  public List<Party> parties;
  public List<Candidate> candidates;
  public List<ContestDescription> contests;
  public List<BallotStyle> ballot_styles;

  public static class AnnotatedString {
    public String annotation;
    public String value;
  }

  public static class BallotStyle extends ElectionObjectBase {
    public List<String> geopolitical_unit_ids;
    public List<String> party_ids;
    public String image_uri;
  }

  public static class Candidate extends ElectionObjectBase {
    public InternationalizedText name;
    public String party_id;
    public String image_uri;
    public Boolean is_write_in = Boolean.FALSE;
  }

  public static class ContactInformation {
    public List<String> address_line;
    public List<AnnotatedString> email;
    public List<AnnotatedString> phone;
    public String name;
  }

  public static class ContestDescription extends ElectionObjectBase {
    public String electoral_district_id;
    public Integer sequence_order;
    public String vote_variation;
    public Integer number_elected;
    public Integer votes_allowed;
    public String name;
    public List<SelectionDescription> ballot_selections;
    public InternationalizedText ballot_title;
    public InternationalizedText ballot_subtitle;
    public List<String> primary_party_ids;
  }

  public static class ElectionObjectBase {
    public String object_id;
  }

  public static class GeopoliticalUnit extends ElectionObjectBase {
    public String name;
    public String type;
    public ContactInformation contact_information;
  }

  public static class InternationalizedText {
    public List<Language> text;
  }

  public static class Language {
    public String value;
    public String language;
  }

  public static class Party extends ElectionObjectBase {
    public InternationalizedText name;
    public String abbreviation;
    public String color;
    public String logo_uri;
  }

  public static class SelectionDescription extends ElectionObjectBase {
    public String candidate_id;
    public Integer sequence_order;
  }

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static Manifest deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    ManifestPojo pojo = gson.fromJson(jsonElem, ManifestPojo.class);
    return convert(pojo);
  }

  private static Manifest convert(ManifestPojo pojo) {
    UInt256 crypto = manifestCryptoHash(
            pojo.election_scope_id,
            Manifest.ElectionType.valueOf(pojo.type),
            pojo.start_date,
            pojo.end_date,
            ConvertPojos.convertCollection(pojo.geopolitical_units, ManifestPojo::convertGeopoliticalUnit),
            ConvertPojos.convertCollection(pojo.parties, ManifestPojo::convertParty),
            ConvertPojos.convertCollection(pojo.candidates, ManifestPojo::convertCandidate),
            ConvertPojos.convertCollection(pojo.contests, ManifestPojo::convertContestDescription),
            ConvertPojos.convertCollection(pojo.ballot_styles, ManifestPojo::convertBallotStyle),
            convertInternationalizedText(pojo.name),
            convertContactInformation(pojo.contact_information));

    return new Manifest(
            pojo.election_scope_id,
            pojo.spec_version,
            Manifest.ElectionType.valueOf(pojo.type),
            pojo.start_date,
            pojo.end_date,
            ConvertPojos.convertCollection(pojo.geopolitical_units, ManifestPojo::convertGeopoliticalUnit),
            ConvertPojos.convertCollection(pojo.parties, ManifestPojo::convertParty),
            ConvertPojos.convertCollection(pojo.candidates, ManifestPojo::convertCandidate),
            ConvertPojos.convertCollection(pojo.contests, ManifestPojo::convertContestDescription),
            ConvertPojos.convertCollection(pojo.ballot_styles, ManifestPojo::convertBallotStyle),
            convertInternationalizedText(pojo.name),
            convertContactInformation(pojo.contact_information),
            crypto);
  }

  @Nullable
  private static Manifest.AnnotatedString convertAnnotatedString(@Nullable AnnotatedString pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = hashElements(
            Strings.nullToEmpty(pojo.annotation),
            Strings.nullToEmpty(pojo.value)
    );

    return new Manifest.AnnotatedString(
            Strings.nullToEmpty(pojo.annotation),
            Strings.nullToEmpty(pojo.value),
            crypto);
  }


  @Nullable
  private static Manifest.BallotStyle convertBallotStyle(@Nullable BallotStyle pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = hashElements(
            ConvertPojos.convertCollection(pojo.geopolitical_unit_ids, Strings::nullToEmpty),
            ConvertPojos.convertCollection(pojo.party_ids, Strings::nullToEmpty),
            Strings.emptyToNull(pojo.image_uri)
    );
    return new Manifest.BallotStyle(pojo.object_id,
            ConvertPojos.convertCollection(pojo.geopolitical_unit_ids, Strings::nullToEmpty),
            ConvertPojos.convertCollection(pojo.party_ids, Strings::nullToEmpty),
            Strings.emptyToNull(pojo.image_uri),
            crypto);
  }

  @Nullable
  private static Manifest.Candidate convertCandidate(@Nullable Candidate pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = hashElements(
            pojo.object_id,
            convertInternationalizedText(pojo.name),
            pojo.party_id,
            pojo.image_uri
    );
    return new Manifest.Candidate(
            pojo.object_id,
            convertInternationalizedText(pojo.name),
            pojo.party_id,
            pojo.image_uri,
            pojo.is_write_in != null && pojo.is_write_in,
            crypto);
  }

  @Nullable
  private static Manifest.ContactInformation convertContactInformation(@Nullable ContactInformation pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = hashElements(
            ConvertPojos.convertCollection(pojo.address_line, Strings::nullToEmpty),
            ConvertPojos.convertCollection(pojo.email, ManifestPojo::convertAnnotatedString),
            ConvertPojos.convertCollection(pojo.phone, ManifestPojo::convertAnnotatedString),
            pojo.name
            );
    return new Manifest.ContactInformation(
            ConvertPojos.convertCollection(pojo.address_line, Strings::nullToEmpty),
            ConvertPojos.convertCollection(pojo.email, ManifestPojo::convertAnnotatedString),
            ConvertPojos.convertCollection(pojo.phone, ManifestPojo::convertAnnotatedString),
            pojo.name,
            crypto);
  }

  @Nullable
  private static Manifest.ContestDescription convertContestDescription(@Nullable ContestDescription pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = contestDescriptionCryptoHash(
            pojo.object_id,
            pojo.sequence_order,
            pojo.electoral_district_id,
            Manifest.VoteVariationType.valueOf(pojo.vote_variation),
            pojo.number_elected,
            pojo.votes_allowed == null ? pojo.number_elected : pojo.votes_allowed,
            pojo.name,
            ConvertPojos.convertCollection(pojo.ballot_selections, ManifestPojo::convertSelectionDescription),
            convertInternationalizedText(pojo.ballot_title),
            convertInternationalizedText(pojo.ballot_subtitle),
            pojo.primary_party_ids == null ? emptyList() : pojo.primary_party_ids
    );
    return new Manifest.ContestDescription(
            pojo.object_id,
            pojo.sequence_order,
            pojo.electoral_district_id,
            Manifest.VoteVariationType.valueOf(pojo.vote_variation),
            pojo.number_elected,
            pojo.votes_allowed == null ? pojo.number_elected : pojo.votes_allowed,
            pojo.name,
            ConvertPojos.convertCollection(pojo.ballot_selections, ManifestPojo::convertSelectionDescription),
            convertInternationalizedText(pojo.ballot_title),
            convertInternationalizedText(pojo.ballot_subtitle),
            pojo.primary_party_ids == null ? emptyList() : pojo.primary_party_ids,
            crypto);
  }

  @Nullable
  private static Manifest.GeopoliticalUnit convertGeopoliticalUnit(@Nullable GeopoliticalUnit pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = hashElements(
            pojo.object_id,
            pojo.name,
            pojo.type,
            convertContactInformation(pojo.contact_information)
    );
    return new Manifest.GeopoliticalUnit(
            pojo.object_id,
            pojo.name,
            Manifest.ReportingUnitType.valueOf(pojo.type),
            convertContactInformation(pojo.contact_information),
            crypto);
  }

  private static Manifest.InternationalizedText convertInternationalizedText(@Nullable InternationalizedText pojo) {
    if (pojo == null) {
      return new Manifest.InternationalizedText(emptyList(), hashElements(emptyList()));
    }
    UInt256 crypto = hashElements(
            ConvertPojos.convertCollection(pojo.text, ManifestPojo::convertLanguage)
    );
    return new Manifest.InternationalizedText(
            ConvertPojos.convertCollection(pojo.text, ManifestPojo::convertLanguage),
            crypto);
  }

  @Nullable
  private static Manifest.Language convertLanguage(@Nullable Language pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = hashElements(
            Strings.nullToEmpty(pojo.value),
            Strings.nullToEmpty(pojo.language)
    );
    return new Manifest.Language(
            Strings.nullToEmpty(pojo.value),
            Strings.nullToEmpty(pojo.language),
            crypto);
  }

  @Nullable
  private static Manifest.Party convertParty(@Nullable Party pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = hashElements(
            pojo.object_id,
            convertInternationalizedText(pojo.name),
            pojo.abbreviation,
            pojo.color,
            pojo.logo_uri
            );
    return new Manifest.Party(
            pojo.object_id,
            convertInternationalizedText(pojo.name),
            pojo.abbreviation,
            pojo.color,
            pojo.logo_uri,
            crypto);
  }

  @Nullable
  private static Manifest.SelectionDescription convertSelectionDescription(@Nullable SelectionDescription pojo) {
    if (pojo == null) {
      return null;
    }
    UInt256 crypto = selectionDescriptionCryptoHash(
            pojo.object_id,
            pojo.sequence_order,
            pojo.candidate_id
            );
    return new Manifest.SelectionDescription(
            pojo.object_id,
            pojo.sequence_order,
            pojo.candidate_id,
            crypto
    );
  }

}