.globl _start

.data
number: .byte 123       # переменная number в секции .data

.text
_start:
    movb $67, %al      # помещаем в AL число 67
    movb %al, number   # помещаем число из AL в переменную number
    movq number, %rdi  # помещаем число значение переменной number в RDI
    movq $60, %rax
    syscall