import java.nio.ByteBuffer;

/**
 * Testing arrays for binary debug view. 
 */
public class Test {

    public static void main(String[] params) {
        boolean[] booleanTestArray = new boolean[10241024];
        Boolean[] BooleanTestArray = new Boolean[10241024];
        byte[] byteTestArray = new byte[10241024];
        Byte[] byteTestArray2 = new Byte[10241024];
        char[] charTestArray = new char[4096];
        Character[] characterTestArray = new Character[4096];
        short[] shortTestArray = new short[10241024];
        int[] intTestArray = new int[10241024];
        Integer[] intTestArray2 = new Integer[10241024];
        long[] longTestArray = new long[10241024];
        float[] floatTestArray = new float[10241024];
        double[] doubleTestArray = new double[10241024];

        for (int i = 0; i < 4096; i++) {
            booleanTestArray[i * 8] = (i & 0x80) > 0;
            booleanTestArray[i * 8 + 1] = (i & 0x40) > 0;
            booleanTestArray[i * 8 + 2] = (i & 0x20) > 0;
            booleanTestArray[i * 8 + 3] = (i & 0x10) > 0;
            booleanTestArray[i * 8 + 4] = (i & 0x8) > 0;
            booleanTestArray[i * 8 + 5] = (i & 0x4) > 0;
            booleanTestArray[i * 8 + 6] = (i & 0x2) > 0;
            booleanTestArray[i * 8 + 7] = (i & 0x1) > 0;
            byteTestArray[i] = (byte) (i & 0xff);
            byteTestArray2[i] = (byte) (i & 0xff);
            shortTestArray[i] = (short) i;
            charTestArray[i] = (char) (i & 0xff);
            intTestArray[i] = i;
            intTestArray2[i] = i;
            longTestArray[i] = i;
            floatTestArray[i] = i;
            doubleTestArray[i] = i;
        }

        byteTestArray2[1] = null;

        byte byteValue = 100;
        int intValue = 100;
        long longValue = 100l;
        float floatValue = 100f;
        double doubleValue = 100d;
        char charValue = 'X';

        Byte byteValueObj = 100;
        Integer intValueObj = 100;
        Long longValueObj = 100l;
        Float floatValueObj = 100f;
        Double doubleValueObj = 100d;
        Character charValueObj = 'X';
        String stringValueObj = "TEST";

        ByteBuffer bb = ByteBuffer.allocate(100);

        // putting the int to byte typecast value
        // in ByteBuffer using putInt() method
        bb.put((byte)20);
        bb.put((byte)30);
        bb.put((byte)40);
        bb.put((byte)50);
        bb.rewind();

        String test = "TEST";
        System.out.println(test);
    }
}