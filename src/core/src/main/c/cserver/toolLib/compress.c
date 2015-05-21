/* **********************************************************************
 * 
 * <copyright>
 * 
 *  BBN Technologies, a Verizon Company
 *  10 Moulton Street
 *  Cambridge, MA 02138
 *  (617) 873-8000
 * 
 *  Copyright (C) BBNT Solutions LLC. All rights reserved.
 * 
 * </copyright>
 * **********************************************************************
 * 
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/compress.c,v $
 * $RCSfile: compress.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/*
 * compress.c
 * 
 *      File compression code derived from compress 4.0, the public
 *      domain file compression utility.  Modified to strip out variants
 *      we will not need, use Diamond style and throw exceptions 
 *      instead of exiting on error.
 */

#include <stdio.h>
#include <ctype.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>

#include "style.h"
#include "error_hand.h"

extern int errno;

/* #define MAC_OS_X */

#ifdef MAC_OS_X
    extern __const int sys_nerr; /* perror(3) external variables */
    extern __const char *__const sys_errlist[];
#else
    extern char *sys_errlist[];
    extern int sys_nerr;
#endif


#ifndef min
#define min(a,b)        ((a>b) ? b : a)
#endif

/* 
 * Various defines taken from the sources to compress version 4.0; since
 * we don't have to run on all the machines compress does, we have stripped
 * all of the options that aren't required for our purposes.
 */

#define BITS            16
#define HSIZE           69001
#define INIT_BITS       9               /* initial number of bits/code */
#define MAXCODE(bits)   ((1 << (bits)) - 1)
#define MAXMAXCODE      (1L << BITS)    /* should NEVER generate this */
#define BIT_MASK        0x1f
#define BLOCK_MASK      0x80
#define CHECK_GAP       10000                  /* ratio check interval */

typedef long            code_t;

/*
 * the next two codes should not be changed lightly, as they must not
 * lie within the contiguous general code space.
 */

#define FIRST   257                    /* first free entry */
#define CLEAR   256                    /* table clear output code */

/*
 * To save much memory, we overlay the table used by compress() with those
 * used by decompress().  The tab_prefix table is the same size and type
 * as the codetab.  The tab_suffix table needs 2**BITS characters.  We
 * get this from the beginning of htab.  The output stack uses the rest
 * of htab, and contains characters.  There is plenty of room for any
 * possible stack (stack used to be 8000 characters).
 */

#define htabof(i)               htab[i]
#define codetabof(i)            codetab[i]
#define tab_prefixof(i)         codetabof(i)
#define tab_suffixof(i)         ((u_char *)(htab))[i]
#define de_stack                ((u_char *)&tab_suffixof(1<<BITS))

static u_char   g_magic[] = {"\037\235"}; /* 1F 9D */

static int     n_bits;                  /* number of bits/code */
static code_t   maxcode;                /* maximum code, given n_bits */

static long     htab[HSIZE];
static u_short  codetab[HSIZE];

static code_t   free_ent;               /* first unused entry */
static int      offset;
static long     in_count;               /* length of input */
static long     bytes_out;              /* length of compressed output */

/*
 * block compression parameters -- after all codes are used up,
 * and compression rate changes, start over.
 */

static int      clear_flg;
static long     ratio;
static long     checkpoint;

static FILE     *in_fp;
static FILE     *out_fp;

static code_t   getcode();
#ifdef DO_COMPRESS
static void     output();
static void     clear_hash();
static void     clear_block();
#endif



/******************************************************************************
 * decompress(): decompress input file pointer to output file pointer
 * 
 *      May throw EX_FAILURE if bad input is supplied, or EX_WRITE
 *      if write errors occur on output.
 * 
 * Lifted from the public domain Compress 4.0
 *
 */

int decompress(in, out, out_ptr, out_bytes)
FILE *in;
FILE *out;
unsigned char *out_ptr;
int out_bytes;
{
    register u_char *stackp;
    register int finchar;
    register code_t code, oldcode, incode;
    int usebits;
    int bytes_written = 0;
    int c;
    
    /* 
     * Make sure input is compressed.
     */
     
    in_fp = in;
    out_fp = out;
    
    if (((c = getc(in_fp)) != (g_magic[0] & 0xFF)) || 
                                (getc(in_fp) != (g_magic[1] & 0xFF)))
    {   
        if (c == EOF)
            WARNING_MESSAGE("Truncated file passed to decompress.");
        else
            WARNING_MESSAGE("Input to decompress not in compressed format.");
        return -1;
    }

    usebits = getc(in_fp) & BIT_MASK;
    if (usebits != BITS)
    {   
        sprintf(msgBuf, 
           "Expected input with %d bit compression, but got %d bit instead.", 
           BITS, usebits);
        WARNING_MESSAGE(msgBuf);
    }
    
    /*
     * As above, initialize the first 256 entries in the table. 
     */

    maxcode = MAXCODE(n_bits = INIT_BITS);
    for (code = 255; code >= 0; code--)
    {
        tab_prefixof(code) = 0;
        tab_suffixof(code) = (u_char) code;
    }
    clear_flg = 0;
    free_ent = FIRST;

    finchar = oldcode = getcode();
    if (oldcode == -1)                 /* EOF already? */
        return(0);                     /* Get out of here */

    bytes_written++;
    if (out_fp != NULL) {
        putc((char)finchar, out_fp);   /* first code must be 8 bits = char */
        /* t_ferror(out_fp); */
    }
    else if (bytes_written < out_bytes)
    {
        *out_ptr++ = (unsigned char)finchar;
    }
    else
    {
        WARNING_MESSAGE("No more room to decompress into buffer, finchar.");
        return -1;
    }

    stackp = de_stack;

    while ((code = getcode()) > -1)
    {
        if ((code == CLEAR) && BLOCK_MASK)
        {
            for (code = 255; code >= 0; code--)
                tab_prefixof(code) = 0;
            clear_flg = 1;
            free_ent = FIRST - 1;
            if ((code = getcode()) == -1)       /* O, untimely death! */
                break;
        }
        incode = code;
        /*
         * Special case for KwKwK string. 
         */
        if (code >= free_ent)
        {
            *stackp++ = finchar;
            code = oldcode;
        }

        /*
         * Generate output characters in reverse order 
         */
        while (code >= 256)
        {
            *stackp++ = tab_suffixof(code);
            code = tab_prefixof(code);
        }
        *stackp++ = finchar = tab_suffixof(code);

        /*
         * And put them out in forward order 
         */
        if (out_fp != NULL)
        {
            do
            {
                putc(*--stackp, out_fp);
                bytes_written++;
            } while (stackp > de_stack);
        }
        else
        {
            do
            {
                if (++bytes_written > out_bytes)
                {
                    WARNING_MESSAGE(
                        "No more room to decompress into buffer, stackp.");
                }
                *out_ptr++ = *--stackp;
            } while (stackp > de_stack);
        }

        /*
         * Generate the new entry. 
         */
        if ((code = free_ent) < MAXMAXCODE)
        {
            tab_prefixof(code) = (u_short) oldcode;
            tab_suffixof(code) = finchar;
            free_ent = code + 1;
        }
        /*
         * Remember previous code. 
         */
        oldcode = incode;
    }
    if (out_fp != NULL)
        fflush(out_fp);  /* Flush */
    return (bytes_written);
}


/******************************************************************************
 * getcode(): Read one code from input file pointer.  If EOF, return -1.
 */


static char buf[BITS];

#ifndef vax
u_char lmask[9] = {0xff, 0xfe, 0xfc, 0xf8, 0xf0, 0xe0, 0xc0, 0x80, 0x00};
u_char rmask[9] = {0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff};
#endif                                 /* vax */

static code_t getcode()
{
    /*
     * On the VAX, it is important to have the register declarations in
     * exactly the order given, or the asm will break. 
     */
    register code_t code;
    static int gc_offset = 0, size = 0;
    static u_char gc_buf[BITS];
    register int r_off, bits;
    register u_char *bp = gc_buf;

    if (clear_flg > 0 || gc_offset >= size || free_ent > maxcode)
    {
        /*
         * If the next entry will be too big for the current code size, then
         * we must increase the size.  This implies reading a new buffer
         * full, too. 
         */
        if (free_ent > maxcode)
        {
            n_bits++;
            if (n_bits == BITS)
                maxcode = MAXMAXCODE;  /* won't get any bigger now */
            else
                maxcode = MAXCODE(n_bits);
        }
        if (clear_flg > 0)
        {
            maxcode = MAXCODE(n_bits = INIT_BITS);
            clear_flg = 0;
        }
        size = fread((char *)gc_buf, 1, n_bits, in_fp);
        if (size <= 0)
            return -1;                 /* end of file */
        gc_offset = 0;
        /* Round size down to integral number of codes */
        size = (size << 3) - (n_bits - 1);
    }
    r_off = gc_offset;
    bits = n_bits;
#ifdef vax
    asm("extzv   r10,r9,(r8),r11");
#else                                  /* not a vax */
    /*
     * Get to the first byte. 
     */
    bp += (r_off >> 3);
    r_off &= 7;
    /* Get first part (low order bits) */
    code = (*bp++ >> r_off);
    bits -= (8 - r_off);
    r_off = 8 - r_off;                 /* now, offset into code word */
    /* Get any 8 bit parts in the middle (<=1 for up to 16 bits). */
    if (bits >= 8)
    {
        code |= *bp++ << r_off;
        r_off += 8;
        bits -= 8;
    }
    /* high order bits. */
    code |= (*bp & rmask[bits]) << r_off;
#endif                                 /* vax */
    gc_offset += n_bits;

    return code;
}


#ifdef DO_COMPRESS
        
/*****************************************************************************
 * output(): Output the supplied code to the supplied file pointer;
 *           Supplying a code of -1 means end of file.
 *
 * Assumptions:
 *      Chars are 8 bits long.
 *****************************************************************************/

static void output(code)
code_t code;
{
    /*
     * On the VAX, it is important to have the register declarations in
     * exactly the order given, or the asm will break. 
     */
    register int r_off = offset, bits = n_bits;
    register char *bp = buf;

    if (code >= 0)
    {
#ifdef vax
        /*
         * VAX DEPENDENT!! Implementation on other machines is below. 
         *
         * Translation: Insert BITS bits from the argument starting at offset
         * bits from the beginning of buf. 
         */
        0;              /* Work around for pcc -O bug with asm and if stmt */
        asm("insv       4(ap),r11,r10,(r9)");
#else                                  /* not a vax */
        /* 
         * byte/bit numbering on the VAX is simulated by the following code
         */

        /*
         * Get to the first byte. 
         */
        bp += (r_off >> 3);
        r_off &= 7;
        /*
         * Since code is always >= 8 bits, only need to mask the first hunk
         * on the left. 
         */
        *bp = (*bp & rmask[r_off]) | (code << r_off) & lmask[r_off];
        bp++;
        bits -= (8 - r_off);
        code >>= 8 - r_off;
        /* Get any 8 bit parts in the middle (<=1 for up to 16 bits). */
        if (bits >= 8)
        {
            *bp++ = code;
            code >>= 8;
            bits -= 8;
        }
        /* Last bits. */
        if (bits)
            *bp = code;
#endif                                 /* vax */
        offset += n_bits;
        if (offset == (n_bits << 3))
        {
            bp = buf;
            bits = n_bits;
            bytes_out += bits;
            do
                putc(*bp++, out_fp);
            while (--bits);
            offset = 0;
        }

        /*
         * If the next entry is going to be too big for the code size, then
         * increase it, if possible. 
         */
        if (free_ent > maxcode || (clear_flg > 0))
        {
            /*
             * Write the whole buffer, because the input side won't discover
             * the size increase until after it has read it. 
             */
            if (offset > 0)
            {
                if (fwrite(buf, 1, n_bits, out_fp) != n_bits)
                    t_write_error();
                bytes_out += n_bits;
            }
            offset = 0;

            if (clear_flg)
            {
                maxcode = MAXCODE(n_bits = INIT_BITS);
                clear_flg = 0;
            }
            else
            {
                n_bits++;
                if (n_bits == BITS)
                    maxcode = MAXMAXCODE;
                else
                    maxcode = MAXCODE(n_bits);
            }
        }
    }
    else
    {
        /*
         * At EOF, write the rest of the buffer. 
         */
        if (offset > 0)
            fwrite(buf, 1, (offset + 7) / 8, out_fp);
        bytes_out += (offset + 7) / 8;
        offset = 0;
        t_fflush(out_fp);  /* Flush, and throw EX_WRITE on error */
    }
}

/******************************************************************************
 * clear_block(): Clear hash table for block compression.  
 */
 
static void clear_block()               /* table clear for block compress */
{
    register long int rat;

    checkpoint = in_count + CHECK_GAP;
    if (in_count > 0x007fffff)
    {                                  /* shift will overflow */
        rat = bytes_out >> 8;
        if (rat == 0)
        {                              /* Don't divide by zero */
            rat = 0x7fffffff;
        }
        else
        {
            rat = in_count / rat;
        }
    }
    else
    {
        rat = (in_count << 8) / bytes_out;      /* 8 fractional bits */
    }
    if (rat > ratio)
    {
        ratio = rat;
    }
    else
    {
        ratio = 0;
        clear_hash((long)HSIZE);
        free_ent = FIRST;
        clear_flg = 1;
        output((code_t)CLEAR);
    }
}


/******************************************************************************
 * clear_hash(): clear and reset the hash table for codes.
 */

static void clear_hash(hsize)
register long hsize;
{
    register long *htab_p = htab + hsize;
    register long i;
    register long m1 = -1;

    i = hsize - 16;
    do
    {                                  /* might use Sys V memset(3) here */
        *(htab_p - 16) = m1;
        *(htab_p - 15) = m1;
        *(htab_p - 14) = m1;
        *(htab_p - 13) = m1;
        *(htab_p - 12) = m1;
        *(htab_p - 11) = m1;
        *(htab_p - 10) = m1;
        *(htab_p - 9) = m1;
        *(htab_p - 8) = m1;
        *(htab_p - 7) = m1;
        *(htab_p - 6) = m1;
        *(htab_p - 5) = m1;
        *(htab_p - 4) = m1;
        *(htab_p - 3) = m1;
        *(htab_p - 2) = m1;
        *(htab_p - 1) = m1;
        htab_p -= 16;
    } while ((i -= 16) >= 0);
    for (i += 16; i > 0; i--)
        *--htab_p = m1;
}

/*
 * compress(): compress input file pointer to output file pointer
 *
 *      May throw EX_WRITE if write errors are encountered.
 *
 * Lifted almost completely from Compress 4.0 -- thanks to all those
 * who wrote compress and put it in the public domain.
 */

/* VARARGS 2 */
void compress(in, out, in_ptr, in_bytes)
FILE *in;
FILE *out;
unsigned char *in_ptr;
int in_bytes;
{
    register long fcode;
    register code_t i = 0;
    register int c;
    register code_t ent;
    register int disp;
    register int hshift;

    in_fp = in;
    out_fp = out;

    putc(g_magic[0], out_fp);
    putc(g_magic[1], out_fp);
    putc((char)(BITS | BLOCK_MASK), out_fp);
    t_ferror(out_fp);  /* Throw EX_WRITE on error */
    
    offset = 0;
    bytes_out = 3;                     /* includes 3-byte header mojo */
    clear_flg = 0;
    ratio = 0;
    in_count = 1;
    checkpoint = CHECK_GAP;
    maxcode = MAXCODE(n_bits = INIT_BITS);
    free_ent = FIRST;

    if (in_fp != NULL)
        ent = getc(in_fp);
    else if (--in_bytes >= 0)
        ent = *in_ptr++;
    else
        throwv(EX_FAILURE, "Empty source buffer passed to compress.");
    
    hshift = 0;
    for (fcode = (long)HSIZE; fcode < 65536L; fcode *= 2L)
        hshift++;
    hshift = 8 - hshift;               /* set hash code range bound */

    clear_hash((long)HSIZE);

    while (in_fp != NULL && (c = getc(in_fp)) != EOF
                || --in_bytes >= 0 && ((c = *in_ptr++), TRUE))
    {
        in_count++;
        fcode = (long) (((long) c << BITS) + ent);
        i = ((c << hshift) ^ ent);     /* xor hashing */

        if (htabof(i) == fcode)
        {
            ent = codetabof(i);
            continue;
        }
        else if ((long) htabof(i) < 0) /* empty slot */
            goto nomatch;
        disp = HSIZE - i;              /* secondary hash (after G. Knott) */
        if (i == 0)
            disp = 1;
probe:
        if ((i -= disp) < 0)
            i += HSIZE;

        if (htabof(i) == fcode)
        {
            ent = codetabof(i);
            continue;
        }
        if ((long) htabof(i) > 0)
            goto probe;
nomatch:
        output((code_t)ent);
        ent = c;
        if (free_ent < MAXMAXCODE)
        {
            codetabof(i) = free_ent++; /* code -> hashtable */
            htabof(i) = fcode;
        }
        else if ((long) in_count >= checkpoint && BLOCK_MASK)
            clear_block();
    }
    /*
     * Put out the final code. 
     */
    output((code_t) ent);
    output((code_t) -1);
}
#endif
