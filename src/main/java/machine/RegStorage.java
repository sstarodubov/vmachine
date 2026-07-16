package machine;

import java.nio.ByteBuffer;

public final class RegStorage {

    private final ByteBuffer mem;

    public RegStorage() {
        this.mem = ByteBuffer.allocate(128);
    }

    public RegStorage(ByteBuffer byteBuffer) {
        this.mem = byteBuffer;
    }

    public static final int eax = 0; // (Accumulator): для арифметических операций
    public static final int ecx = 4; //(Counter): для хранения счетчика цикла
    public static final int edx = 8; // (Data): для арифметических операций и операций ввода-вывода
    public static final int ebx = 12; //(Base): указатель на данные
    public static final int esp = 16; // (Stack pointer): указатель на верхушку стека
    public static final int ebp = 20; // (Base pointer): указатель на базу стека внутри функции
    public static final int esi = 24; // (Source index): указатель на источник при операциях с массивом
    public static final int edi = 28; //(Destination index): указатель на место назначения в операциях с массивами
    public static final int eip = 32; // указатель адреса следующей инструкции для выполнения

    //flags
    public static final int cf = 36;
    public static final int of = 37;
    public static final int zf = 38;
    public static final int sf = 39;



    public static int registerIdFromName(final String regName) {
        return switch (regName) {
            case "eax" -> 0;
            case "ax", "ah" -> 2;
            case "al" -> 3;
            case "ecx" -> 4;
            case "edx" -> 8;
            case "ebx" -> 12;
            case "esp" -> 16;
            case "ebp" -> 20;
            case "esi" -> 24;
            case "edi" -> 28;
            case "eip" -> 32;
            default -> -1;
        };
    }
    // flags
    public boolean readSF() {
        return mem.get(sf) == 1;
    }

    public void setSF() {
        mem.put(sf, (byte) 1);
    }

    public void clearSF() {
        mem.put(sf, (byte) 0);
    }

    public boolean readCF() {
        return mem.get(cf) == 1;
    }

    public boolean readOF() {
        return mem.get(of) == 1;
    }

    public void setCF() {
        mem.put(cf, (byte) 1);
    }

    public void setOF() {
        mem.put(of, (byte) 1);
    }

    public void clearCF() {
        mem.put(cf, (byte) 0);
    }

    public boolean readZF() {
        return mem.get(zf) == 1;
    }

   public void setZF() {
        mem.put(zf, (byte) 1);
   }

   public void clearZF() {
        mem.put(zf, (byte) 0);
   }

   public void clearOF() {
        mem.put(of, (byte) 0);
    }

    // utils
   public static boolean isEq(final String reg1, final String reg2) {
        return RegStorage.getRegisterSize(reg2) == RegStorage.getRegisterSize(reg1);
   }

    public static boolean isCompatibleMovSemantic(final int operSize, final String regName) {
        return operSize == RegStorage.getRegisterSize(regName);
    }

    public static boolean isCompatibleSize(final int num, final String regName) {
        final int regSize = RegStorage.getRegisterSize(regName);
        return switch (regSize) {
            case 1 -> num >= Byte.MIN_VALUE && num <= Byte.MAX_VALUE;
            case 2 -> num >= Short.MIN_VALUE && num <= Short.MAX_VALUE;
            case 4 -> true;
            default -> false;
        };
    }

    public static int getRegisterSize(final String regName) {
        return switch (regName) {
            case "ax" -> 2;
            case "al", "ah" -> 1;
            default -> 4;
        };
    }


    //registers
    // eip
    public int readEip() {
        return readInt(eip);
    }

    public void writeEip(int val) {
        writeInt(eip, val);
    }

    public void incEip() {
        writeEip(readEip() + 1);
    }

    public void addEip(int k) {
        writeEip(readEip() + k);
    }

    // edi
    public int readEdi() {
        return readInt(edi);
    }

    public void writeEdi(int val) {
        writeInt(edi, val);
    }

    // esi
    public int readEsi() {
        return readInt(esi);
    }

    public void writeEsi(int val) {
        writeInt(esi, val);
    }

    //ebp
    public int readEbp() {
        return readInt(ebp);
    }

    public void writeEbp(int val) {
        writeInt(ebp, val);
    }

    //esp
    public int readEsp() {
        return readInt(esp);
    }

    public void writeEsp(int val) {
        writeInt(esp, val);
    }

    public void addEsp(final int d) {
        final int val = readEsp();
        writeEsp(val + d);
    }

    public void subEsp(final int d) {
        final int val = readEsp();
        writeEsp(val - d);
    }

    public void add4Esp() {
        final int val = readEsp();
        writeEsp(val + 4);
    }

    public void sub4Esp() {
        final int val = readEsp();
        writeEsp(val - 4);
    }

    public void incEsp() {
        final int val = readEsp();
        writeEsp(val + 1);
    }

    public void decEsp() {
        final int val = readEsp();
        writeEsp(val - 1);
    }


    //ebx
    public int readEbx() {
        return readInt(ebx);
    }

    public void writeEbx(int val) {
        writeInt(ebx, val);
    }

    //edx
    public int readEdx() {
        return readInt(edx);
    }

    public void writeEdx(int val) {
        writeInt(edx, val);
    }

    // eax
    public int readEax() {
        return readInt(eax);
    }

    public void writeEax(int val) {
        writeInt(eax, val);
    }

    //ecx
    public int readEcx() {
        return readInt(ecx);
    }

    public void writeEcx(int val) {
        writeInt(ecx, val);
    }


    // access by index
    public void writeInt(int reg, int val) {
        mem.putInt(reg, val);
    }

    public void writeShort(int reg, short val) {
        mem.putShort(reg, val);
    }

    public void writeByte(int reg, byte val) {
        mem.put(reg, val);
    }

    public int readInt(int reg) {
        return mem.getInt(reg);
    }

    public short readShort(int reg) {
        return mem.getShort(reg);
    }

    public byte readByte(int reg) {
        return mem.get(reg);
    }

}
