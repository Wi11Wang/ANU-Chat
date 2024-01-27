package User;

public enum College {
    CASS, CAP, CBE, CECS, CHM, LAW, SCIENCE;

    public static int getIndex(College college) {
        switch (college) {
            case CASS:
                return 1;
            case CAP:
                return 2;
            case CBE:
                return 3;
            case CECS:
                return 4;
            case CHM:
                return 5;
            case LAW:
                return 6;
            case SCIENCE:
                return 7;
        }
        return -1;
    }
}
