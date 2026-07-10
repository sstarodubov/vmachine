package machine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class RegStorageTest {

    RegStorage regStorage;

    @BeforeEach
    void be() {
        regStorage = new RegStorage(ByteBuffer.wrap(new byte[]{
                1, 2, 3, 4, // eax
                5, 6, 7, 8,  // ecx
                11, 12, 13, 14, // edx
                0x1, (byte) 0x89, 0x2, (byte) 0xcd, //ebx
        }));
    }

    @Test
    void test17() {
        regStorage = new RegStorage();
        regStorage.writeEip(10);
        regStorage.addEip(15);
        assertEquals(25, regStorage.readEip());
    }

    @Test
    void test16() {
        regStorage = new RegStorage();
        regStorage.writeEip(10);
        regStorage.incEip();
        assertEquals(11, regStorage.readEip());
    }

    @Test
    void test15() {
        regStorage = new RegStorage();
        regStorage.writeEip(10);
        assertEquals(10, regStorage.readEip());
    }

    @Test
    void test14() {
        regStorage.writeEbx(0xac010203);
        Assertions.assertEquals(0xac010203, regStorage.readEbx());
    }

    @Test
    void test13() {
        Assertions.assertEquals(25756365, regStorage.readEbx());
    }

    @Test
    void test12() {
        regStorage.writeEdx(9);
        Assertions.assertEquals(9, regStorage.readEdx());
    }

    @Test
    void test11() {
        Assertions.assertEquals(185339150, regStorage.readEdx());
    }

    @Test
    void test10() {
        regStorage.writeEcx(15);
        Assertions.assertEquals(15, regStorage.readEcx());
    }

    @Test
    void test9() {
        Assertions.assertEquals(84281096, regStorage.readEcx());
    }

    @Test
    void test1() {
        Assertions.assertEquals(16909060, regStorage.readEax());
    }

    @Test
    void test5() {
        regStorage.writeEax(10);
        Assertions.assertEquals(10, regStorage.readEax());
    }
}