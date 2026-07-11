.globl _start
.text
_start:
    movq $5, %rcx
    movq $0, %rdi
loop:
    addq $2, %rdi     # RDI = RDI + 2
    subq $1, %rcx     # RCX = RCX - 1
    jnz loop          # если флаг нуля НЕ установлен, переход обратно к метке loop

    movq $60, %rax
    syscall