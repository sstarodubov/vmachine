.globl _start

.text
_start:
    movw $1, %rax
    movq $rax, %rdi
    syscall