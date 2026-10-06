; ═══════════════════════════════════════════════════════════════════════
; rle.asm — Run-Length Encoding in x86 Assembly (NASM Syntax)
; ═══════════════════════════════════════════════════════════════════════
;
; PURPOSE:
;   This file provides a conceptual x86 assembly implementation of the
;   RLE compression algorithm. It is included for educational purposes
;   to demonstrate how RLE operates at the hardware/register level.
;
; VIVA KEY POINTS:
;   • SI (Source Index) points to the current position in the input buffer.
;   • DI (Destination Index) points to the current position in the output.
;   • AL (Accumulator Low) holds the current character being processed.
;   • CL (Counter Low) counts consecutive occurrences of the character.
;   • The CMP instruction compares two bytes and sets the Zero Flag (ZF).
;   • JE (Jump if Equal) branches when ZF = 1 (characters match).
;   • JNE (Jump if Not Equal) branches when ZF = 0 (characters differ).
;   • The loop follows the Fetch → Decode → Execute instruction cycle.
;
; REGISTERS USED:
;   AL  — Current character (accumulator)
;   CL  — Run-length counter
;   SI  — Source index (pointer into input string)
;   DI  — Destination index (pointer into output buffer)
;   BX  — Temporary storage for string length
;
; ALGORITHM:
;   1. Load the first character from input into AL.
;   2. Set counter CL = 1.
;   3. Compare the next byte with AL.
;   4. If equal: increment CL, advance SI, repeat step 3.
;   5. If not equal: store AL and CL to output, go to step 1.
;   6. Repeat until all input bytes are processed.
;
; NOTE: This is a flat-model, conceptual listing. It demonstrates the
;       register-level logic and may require a DOS/Linux environment
;       or emulator (e.g., emu8086, NASM + DOSBox) to assemble and run.
; ═══════════════════════════════════════════════════════════════════════

section .data
    ; ── Input string to compress ──
    input_str   db  'AAAABBBCCDDDDDD', 0     ; Null-terminated input
    input_len   equ $ - input_str - 1         ; Length (excluding null)

    ; ── Output buffer (worst case: 2x input length) ──
    output_buf  times 64 db 0                 ; Zeroed output buffer

    ; ── Messages for display ──
    msg_input   db  'Input:  ', 0
    msg_output  db  'Output: ', 0
    newline     db  10, 0                     ; Line feed

section .bss
    ; (No uninitialized data needed)

section .text
    global _start

; ═══════════════════════════════════════════════════════════════════════
; _start — Program entry point
; ═══════════════════════════════════════════════════════════════════════
_start:
    ; ── Initialize pointers ──
    ; FETCH:   Load addresses of input and output buffers into registers
    ; DECODE:  CPU recognizes MOV instruction, source=immediate, dest=SI/DI
    ; EXECUTE: SI ← address of input_str, DI ← address of output_buf
    
    mov     si, input_str       ; SI = pointer to start of input
    mov     di, output_buf      ; DI = pointer to start of output
    mov     bx, input_len       ; BX = total number of bytes to process

; ═══════════════════════════════════════════════════════════════════════
; compress_loop — Main RLE compression loop
; ═══════════════════════════════════════════════════════════════════════
compress_loop:
    ; ── Check if we've processed all bytes ──
    cmp     bx, 0               ; Are there bytes remaining?
    je      done                 ; If BX == 0, jump to done

    ; ── STEP 1: Load current character into AL ──
    ; FETCH:   Read instruction "MOV AL, [SI]" from memory
    ; DECODE:  MOV instruction — load byte at address in SI into AL
    ; EXECUTE: AL ← byte at memory[SI]
    
    mov     al, [si]            ; AL = current character
    mov     cl, 1               ; CL = 1 (we've seen this char once)

; ═══════════════════════════════════════════════════════════════════════
; count_loop — Count consecutive identical characters
; ═══════════════════════════════════════════════════════════════════════
count_loop:
    ; ── Check if next position is within bounds ──
    push    bx                  ; Save remaining count
    sub     bx, 1               ; Temporarily check if more bytes exist
    cmp     bx, 0
    pop     bx                  ; Restore BX
    je      store_pair          ; If no more bytes, store what we have

    ; ── STEP 2: Compare next byte with current character ──
    ; FETCH:   Read instruction "CMP [SI+1], AL" from memory
    ; DECODE:  CMP instruction — subtract AL from byte at [SI+1]
    ;          Result is NOT stored, only flags are updated
    ; EXECUTE: temp = memory[SI+1] - AL
    ;          If temp == 0: ZF ← 1 (characters match)
    ;          If temp != 0: ZF ← 0 (characters differ)
    
    cmp     [si+1], al          ; Compare next byte with AL
    jne     store_pair          ; If not equal (ZF=0), stop counting

    ; ── Characters match: increment counter ──
    ; FETCH:   Read "INC CL" instruction
    ; DECODE:  INC instruction — operand is CL register
    ; EXECUTE: CL ← CL + 1
    
    inc     cl                  ; CL++ (one more consecutive occurrence)
    inc     si                  ; Move source pointer forward
    dec     bx                  ; One fewer byte remaining
    jmp     count_loop          ; Loop back to check next byte

; ═══════════════════════════════════════════════════════════════════════
; store_pair — Write character + count to output buffer
; ═══════════════════════════════════════════════════════════════════════
store_pair:
    ; ── STEP 3: Store the character in output ──
    ; FETCH:   Read "MOV [DI], AL"
    ; DECODE:  MOV instruction — store AL at address in DI
    ; EXECUTE: memory[DI] ← AL (the character, e.g., 'A' = 0x41)
    
    mov     [di], al            ; Output: store the character
    inc     di                  ; DI++ (advance output pointer)

    ; ── STEP 4: Convert count to ASCII digit and store ──
    ; For simplicity, this handles single-digit counts (1-9).
    ; FETCH:   Read "ADD CL, '0'" instruction
    ; DECODE:  ADD instruction — add immediate 0x30 to CL
    ; EXECUTE: CL ← CL + 48 (convert number to ASCII digit)
    ;          Example: count=3 → CL = 3 + 48 = 51 = ASCII '3'
    
    add     cl, '0'             ; Convert count to ASCII digit
    mov     [di], cl            ; Output: store the count digit
    inc     di                  ; DI++ (advance output pointer)

    ; ── Advance to next character in input ──
    inc     si                  ; Move past the last counted byte
    dec     bx                  ; One fewer byte remaining
    jmp     compress_loop       ; Process next character

; ═══════════════════════════════════════════════════════════════════════
; done — Null-terminate output and exit
; ═══════════════════════════════════════════════════════════════════════
done:
    mov     byte [di], 0        ; Null-terminate the output string

    ; ── Exit program (Linux syscall) ──
    mov     eax, 1              ; syscall: exit
    xor     ebx, ebx            ; exit code 0
    int     0x80                ; invoke kernel

; ═══════════════════════════════════════════════════════════════════════
; END OF PROGRAM
;
; SUMMARY OF INSTRUCTION CYCLE FOR RLE:
;
;   ┌──────────────────────────────────────────────────────────┐
;   │  FETCH  → CPU reads the next instruction from memory     │
;   │           using the Program Counter (PC/IP).             │
;   │                                                          │
;   │  DECODE → Control Unit interprets the opcode:            │
;   │           MOV, CMP, INC, DEC, JE, JNE, ADD, JMP         │
;   │                                                          │
;   │  EXECUTE → ALU performs the operation:                   │
;   │           - MOV: transfers data between register/memory  │
;   │           - CMP: subtracts and sets flags (ZF, CF)       │
;   │           - INC/DEC: adds/subtracts 1                    │
;   │           - ADD: arithmetic addition                     │
;   │           - JE/JNE/JMP: updates PC for branching         │
;   └──────────────────────────────────────────────────────────┘
;
; MEMORY LAYOUT EXAMPLE:
;   Before: [0x41][0x41][0x41][0x41][0x42][0x42][0x42]...
;            A     A     A     A     B     B     B
;
;   After:  [0x41][0x34][0x42][0x33]...
;            A     4     B     3
;
;   Each character takes 1 byte (ASCII).
;   RLE replaces N identical bytes with 2 bytes (char + count).
;   Effective when runs are longer than 2 characters.
; ═══════════════════════════════════════════════════════════════════════
