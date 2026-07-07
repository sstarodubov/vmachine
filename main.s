.globl _start
.text
_start:

    movq $11, %rcx  # RCX = 11
    movq %rcx, %rdi  # RDI = 11

    addq %rcx, %rdi  # RDI = RCX + RDI = 11 + 22 = 33
    movq $60, %rax  # RAX = 60
    syscall