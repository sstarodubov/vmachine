       .globl _start
              .data
              nums: .long 1, 2, 3, 4, 5, 6
              count: .long .- nums

              .text
              _start:
                  movl count, %edi    # помещаем в RDI размер массива nums
                  movl $60, %eax
                  syscall