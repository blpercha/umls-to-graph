package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum SemanticGroup {
    ACTI(
            Arrays.asList(SemanticType.T052,
                    SemanticType.T053,
                    SemanticType.T056,
                    SemanticType.T051,
                    SemanticType.T064,
                    SemanticType.T055,
                    SemanticType.T066,
                    SemanticType.T057,
                    SemanticType.T054)),
    ANAT(
            Arrays.asList(SemanticType.T017,
                    SemanticType.T029,
                    SemanticType.T023,
                    SemanticType.T030,
                    SemanticType.T031,
                    SemanticType.T022,
                    SemanticType.T025,
                    SemanticType.T026,
                    SemanticType.T018,
                    SemanticType.T021,
                    SemanticType.T024)),
    CHEM(
            Arrays.asList(SemanticType.T116,
                    SemanticType.T195,
                    SemanticType.T123,
                    SemanticType.T122,
                    SemanticType.T118,
                    SemanticType.T103,
                    SemanticType.T120,
                    SemanticType.T104,
                    SemanticType.T200,
                    SemanticType.T111,
                    SemanticType.T196,
                    SemanticType.T126,
                    SemanticType.T131,
                    SemanticType.T125,
                    SemanticType.T129,
                    SemanticType.T130,
                    SemanticType.T197,
                    SemanticType.T119,
                    SemanticType.T124,
                    SemanticType.T114,
                    SemanticType.T109,
                    SemanticType.T115,
                    SemanticType.T121,
                    SemanticType.T192,
                    SemanticType.T110,
                    SemanticType.T127)),
    CONC(
            Arrays.asList(SemanticType.T185,
                    SemanticType.T077,
                    SemanticType.T169,
                    SemanticType.T102,
                    SemanticType.T078,
                    SemanticType.T170,
                    SemanticType.T171,
                    SemanticType.T080,
                    SemanticType.T081,
                    SemanticType.T089,
                    SemanticType.T082,
                    SemanticType.T079)),
    DEVI(
            Arrays.asList(SemanticType.T203,
                    SemanticType.T074,
                    SemanticType.T075)),
    DISO(
            Arrays.asList(SemanticType.T020,
                    SemanticType.T190,
                    SemanticType.T049,
                    SemanticType.T019,
                    SemanticType.T047,
                    SemanticType.T050,
                    SemanticType.T033,
                    SemanticType.T037,
                    SemanticType.T048,
                    SemanticType.T191,
                    SemanticType.T046,
                    SemanticType.T184,
                    SemanticType.T041)),
    GENE(
            Arrays.asList(SemanticType.T087,
                    SemanticType.T088,
                    SemanticType.T028,
                    SemanticType.T085,
                    SemanticType.T086)),
    GEOG(
            Collections.singletonList(SemanticType.T083)),
    LIVB(
            Arrays.asList(SemanticType.T100,
                    SemanticType.T011,
                    SemanticType.T008,
                    SemanticType.T194,
                    SemanticType.T007,
                    SemanticType.T012,
                    SemanticType.T204,
                    SemanticType.T099,
                    SemanticType.T013,
                    SemanticType.T004,
                    SemanticType.T096,
                    SemanticType.T016,
                    SemanticType.T015,
                    SemanticType.T001,
                    SemanticType.T101,
                    SemanticType.T002,
                    SemanticType.T098,
                    SemanticType.T097,
                    SemanticType.T014,
                    SemanticType.T010,
                    SemanticType.T005)),
    OBJC(
            Arrays.asList(SemanticType.T071,
                    SemanticType.T168,
                    SemanticType.T073,
                    SemanticType.T072,
                    SemanticType.T167)),
    OCCU(
            Arrays.asList(SemanticType.T091,
                    SemanticType.T090)),
    ORGA(
            Arrays.asList(SemanticType.T093,
                    SemanticType.T092,
                    SemanticType.T094,
                    SemanticType.T095)),
    PHEN(
            Arrays.asList(SemanticType.T038,
                    SemanticType.T069,
                    SemanticType.T068,
                    SemanticType.T034,
                    SemanticType.T070,
                    SemanticType.T067)),
    PHYS(
            Arrays.asList(SemanticType.T043,
                    SemanticType.T201,
                    SemanticType.T045,
                    SemanticType.T044,
                    SemanticType.T032,
                    SemanticType.T040,
                    SemanticType.T042,
                    SemanticType.T039)),
    PROC(
            Arrays.asList(SemanticType.T060,
                    SemanticType.T065,
                    SemanticType.T058,
                    SemanticType.T059,
                    SemanticType.T063,
                    SemanticType.T062,
                    SemanticType.T061));

    private final List<SemanticType> types;

    SemanticGroup(List<SemanticType> types) {
        this.types = types;
    }

    private List<SemanticType> getTypes() {
        return types;
    }

    public static SemanticGroup fromType(SemanticType type) {
        for (SemanticGroup group : SemanticGroup.values()) {
            if (group.getTypes().contains(type)) {
                return group;
            }
        }
        return null;
    }

}
