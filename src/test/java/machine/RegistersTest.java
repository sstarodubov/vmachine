package machine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class RegistersTest {

    Registers registers;

    @BeforeEach
    void be() {
        registers = new Registers(ByteBuffer.wrap(new byte[]{
                1, 2, 3, 4, // eax
                5, 6, 7, 8,  // ecx
                11, 12, 13, 14, // edx
                0x1, (byte) 0x89, 0x2, (byte) 0xcd, //ebx
        }));
    }

    @Test
    void test17() {
        registers = new Registers();
        registers.writeEip(10);
        registers.addEip(15);
        assertEquals(25, registers.readEip());
    }

    @Test
    void test16() {
        registers = new Registers();
        registers.writeEip(10);
        registers.incEip();
        assertEquals(11, registers.readEip());
    }

    @Test
    void test15() {
        registers = new Registers();
        registers.writeEip(10);
        assertEquals(10, registers.readEip());
    }

    @Test
    void test14() {
        registers.writeEbx(0xac010203);
        Assertions.assertEquals(0xac010203, registers.readEbx());
    }

    @Test
    void test13() {
        Assertions.assertEquals(25756365, registers.readEbx());
    }

    @Test
    void test12() {
        registers.writeEdx(9);
        Assertions.assertEquals(9, registers.readEdx());
    }

    @Test
    void test11() {
        Assertions.assertEquals(185339150, registers.readEdx());
    }

    @Test
    void test10() {
        registers.writeEcx(15);
        Assertions.assertEquals(15, registers.readEcx());
    }

    @Test
    void test9() {
        Assertions.assertEquals(84281096, registers.readEcx());
    }

    @Test
    void test1() {
        Assertions.assertEquals(16909060, registers.readEax());
    }

    @Test
    void test2() {
        Assertions.assertEquals(772, registers.readAx());
    }

    @Test
    void test3() {
        Assertions.assertEquals(3, registers.readAh());
    }

    @Test
    void test4() {
        Assertions.assertEquals(4, registers.readAl());
    }

    @Test
    void test5() {
        registers.writeEax(10);
        Assertions.assertEquals(10, registers.readEax());
    }

    @Test
    void test6() {
        registers.writeAx((short) 10);
        Assertions.assertEquals(10, registers.readAx());
    }

    @Test
    void test7() {
        registers.writeAh((byte) 10);
        Assertions.assertEquals(10, registers.readAh());
    }


    @Test
    void test8() {
        registers.writeAl((byte) 10);
        Assertions.assertEquals(10, registers.readAl());
    }

}