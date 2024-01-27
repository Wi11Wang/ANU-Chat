package User;

public enum Gender {
    FEMALE, MALE, TRANS, OTHERS;

    public static int getIndex(Gender gender) {
        switch (gender) {
            case FEMALE:
                return 1;
            case MALE:
                return 2;
            case TRANS:
                return 3;
            default:
                return 0;
        }
    }
}
