/* File : perl.i */

/* http://www.swig.org/Doc1.3/Library.html#Library_carrays
* 8.3.2 Passing binary data */
%apply (char *STRING, int LENGTH) { (const void* buffer, int len) };

%include ../swig.i
