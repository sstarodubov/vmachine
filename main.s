.globl _start
.text
_start:
    movq $exit, %rbx    # в регистре RBX адрес метки exit
    movq $11, %rdi
    jmp *%rbx       # переход по адресу, который хранится в регистре RBX
    movq $6, %rdi
exit:
    movq $60, %rax
    syscall