.globl _start
.data

# условная структура
person:
    .quad alice    # адрес строки
    .quad alice_name_size   # размер имени
    .quad 34      # возраст

# смещение компонентов в структуре
.equ NAME_OFFSET, 0
.equ NAME_SIZE_OFFSET, 8
.equ AGE_OFFSET, 16

alice: .ascii "Alice\n"  # имя
alice_name_size= .-alice   # размер имени

.text
_start:
    movq $person, %rbx
    movq NAME_OFFSET(%rbx), %rsi  # в RSI - адрес строки

    movq $1, %rdi        # в RDI - дексриптор вывода в стандартный поток (консоль)

    movq NAME_SIZE_OFFSET(%rbx), %rdx    # в RDX - длина строки

    movq $1, %rax        # в RAX - номер функции для вывода в поток

    syscall              # вызываем функцию Linux

    movq AGE_OFFSET(%rbx), %rdi  # в RDI - возраст

    movq $60, %rax
    syscall