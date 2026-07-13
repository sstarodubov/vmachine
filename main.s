        .globl _start

        .section .text
        number: .long 123   # определяем объект number внутри секции .text

        _start:
            movl $number, %edx   # RDX = number
            movl %edx, %edi     # RDI = RDX = number
            movl $60, %eax
            syscall