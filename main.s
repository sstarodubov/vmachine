.globl _start

.equ _a, -8
.equ _b, -16
.text
_start:
    movq $11, %rdi      # в RDI параметр для функции sum
    call sum            # после вызова в RAX - результат сложения
    movq %rax, %rdi     # помещаем результат в RDI

    movq $60, %rax      # RDI = 16
    syscall

sum:
    pushq %rbp              # сохраняем старое значение RBP в стек
    movq %rsp, %rbp         # копируем текущий адрес из RSP в RBP
    subq $16, %rsp          # выделяем место для двух переменных по 8 байт

    movq $8, _a(%rbp)       # По адресу -8(%rbp) - локальная переменная _a
    movq %rdi, _b(%rbp)    # По адресу -16(%rbp) - локальная переменная _b

    movq _a(%rbp), %rax     # в RAX значение из _a  - первая локальная переменная
    addq _b(%rbp), %rax    # RAX = RAX + _b - вторая локальная переменная

    movq %rbp, %rsp         # восстанавливаем ранее сохраненное значение RSP
    popq %rbp               # восстанавливем RBP
    ret