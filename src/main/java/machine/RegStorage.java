package machine;

import java.nio.ByteBuffer;

public final class RegStorage {

    private final ByteBuffer mem;

    public RegStorage() {
        this.mem = ByteBuffer.allocate(/*registers*/9 * /*size of reg*/4);
    }

    public RegStorage(ByteBuffer byteBuffer) {
        this.mem = byteBuffer;
    }

    public static final int eax = 0; // (Accumulator): для арифметических операций
    public static final int ax = 2;
    public static final int ah = 2;
    public static final int al = 3;

    public static final int ecx = 4; //(Counter): для хранения счетчика цикла
    public static final int edx = 8; // (Data): для арифметических операций и операций ввода-вывода
    public static final int ebx = 12; //(Base): указатель на данные
    public static final int esp = 16; // (Stack pointer): указатель на верхушку стека
    public static final int ebp = 20; // (Base pointer): указатель на базу стека внутри функции
    public static final int esi = 24; // (Source index): указатель на источник при операциях с массивом
    public static final int edi = 28; //(Destination index): указатель на место назначения в операциях с массивами
    public static final int eip = 32; // указатель адреса следующей инструкции для выполнения

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

    public void wrieEdi(int val) {
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

    public short readAx() {
        return readShort(ax);
    }

    public byte readAh() {
        return readByte(ah);
    }

    public byte readAl() {
        return readByte(al);
    }

    public void writeEax(int val) {
        writeInt(eax, val);
    }

    public void writeAx(short val) {
        writeShort(ax, val);
    }

    public void writeAh(byte val) {
        writeByte(ah, val);
    }

    public void writeAl(byte val) {
        writeByte(al, val);
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
