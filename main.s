.globl _start

.data
    num: .quad 45
.text
_start:
    call sum
    movq %rax, %rdi     # RDI = RAX = 25
    addq num, %rdi     # RDI = RDI + num = 25 + 45 = 70

    movq $60, %rax
    syscall

sum:
    pushq num        # сохраняем значение num в стек
    movq $15, num
    movq $10, %rax
    addq num, %rax     # RAX = RAX + num = 10 + 15 = 25
    popq num         # восстанавливаем num из стека
    ret