/* File : csharp.i 
* http://www.swig.org/Doc1.3/CSharp.html
*/

%define %cs_marshal_array(TYPE, CSTYPE)
%typemap(ctype) TYPE[] "void*"
%typemap(imtype,
inattributes="[MarshalAs(UnmanagedType.LPArray)]") TYPE[] "CSTYPE[]"
%typemap(cstype) TYPE[] "CSTYPE[]"
%typemap(in) TYPE[] %{ $1 = (TYPE*)$input; %}
%typemap(csin) TYPE[] "$csinput"
%enddef

// Mapping void* as IntPtr
%typemap(ctype)  void * "void *"
%typemap(imtype) void * "IntPtr"
%typemap(cstype) void * "IntPtr"
%typemap(csin)   void * "$csinput"
%typemap(in)     void * %{ $1 = $input; %}
%typemap(out)    void * %{ $result = $1; %}
%typemap(csout)  void * { return $imcall; }
%typemap(csdirectorin) void * "$iminput"

%include ../swig.i
