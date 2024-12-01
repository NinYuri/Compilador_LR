package compilador;
import java.io.*;
import static compilador.Tokens.*;


%%
%class Lexer
%type Tokens
%line
%column
L=[a-zA-Z_]+
D=-?[0-9]+|-?(([0-9]+[.]?[0-9]*)|([.][0-9]+))(e[+-]?[0-9]+)?
CA=\"(\\.|[^\"])*\"
CAR='([^'\\]|\\\\[btnfr"'\\"\\\\])'
espacio=[ ]+
espa=[\t]+
esp=[\r]+
salto=[\n]
%{
    public String lexeme;
    Analisis c=new Analisis();
    int estado = 0;
    int nu=0;
%}
%%

"//" .* { /* Ignore */ }
"/*" [^*] ~"*/" | "/*" "*"+ "/" { /* Ignore */ }
{espacio} {/*Ignore*/}
{espa} {/*Ignore*/}
{esp} {/*Ignore*/}
{salto} {/*Ignore*/}

<YYINITIAL> "class" {estado = 5; c.linea = yyline; lexeme = yytext(); return classType;}
<YYINITIAL> "void" {estado = 6; c.linea = yyline; lexeme = yytext(); return voidType;}
<YYINITIAL> "main" {c.linea = yyline; lexeme = yytext(); return mainType;}
<YYINITIAL> "public" {c.linea = yyline; lexeme = yytext(); return publicType;}
<YYINITIAL> "private" {c.linea = yyline; lexeme = yytext(); return privateType;}
<YYINITIAL> "int" {estado = 1; c.linea = yyline; lexeme = yytext(); return intType;}
<YYINITIAL> "float" {estado = 2; c.linea = yyline; lexeme = yytext(); return floatType;}
<YYINITIAL> "char" {estado = 3; c.linea = yyline; lexeme = yytext(); return charType;}
<YYINITIAL> "String" {estado = 4; c.linea= yyline; lexeme = yytext(); return stringType;}
<YYINITIAL> "," {c.linea= yyline; lexeme = yytext(); return coma;}
<YYINITIAL> ";" {estado = 0; c.linea= yyline; lexeme = yytext(); return semicolon;}
<YYINITIAL> "+" {c.linea= yyline; lexeme = yytext(); return plus;}
<YYINITIAL> "-" {c.linea= yyline; lexeme = yytext(); return minus;}
<YYINITIAL> "*" {c.linea= yyline; lexeme = yytext(); return mult;}
<YYINITIAL> "/" {c.linea= yyline; lexeme = yytext(); return div;}
<YYINITIAL> "=" {c.linea= yyline; lexeme = yytext(); return equal;}
<YYINITIAL> "<" {c.linea = yyline; lexeme = yytext(); return less_than;}
<YYINITIAL> ">" {c.linea = yyline; lexeme = yytext(); return greater_than;}
<YYINITIAL> "<=" {c.linea = yyline; lexeme = yytext(); return less_or_equals;}
<YYINITIAL> ">=" {c.linea = yyline; lexeme = yytext(); return greater_or_equals;}
<YYINITIAL> "!=" {c.linea = yyline; lexeme = yytext(); return different_to;}
<YYINITIAL> "==" {c.linea = yyline; lexeme = yytext(); return equals_to;}
<YYINITIAL> "(" {c.linea = yyline; lexeme = yytext(); return open_parenth;}
<YYINITIAL> ")" {c.linea = yyline; lexeme = yytext(); return close_parenth;}
<YYINITIAL> "{" {estado = 0; c.linea = yyline; lexeme = yytext(); return open_key;}
<YYINITIAL> "}" {c.linea = yyline; lexeme = yytext(); return close_key;}
<YYINITIAL> "if" {c.linea = yyline; lexeme = yytext(); return if_keyword;}
<YYINITIAL> "while" {c.linea = yyline; lexeme = yytext(); return while_keyword;}
<YYINITIAL> "read" {c.linea = yyline; lexeme = yytext(); return read;}
<YYINITIAL> "print" {c.linea = yyline; lexeme = yytext(); return print;}
<YYINITIAL> {L}({L}{D})* {if(estado == 1) {
        c.linea = yyline;
        lexeme = yytext();
        return idI;
    } else if(estado == 2) {
        c.linea = yyline;
        lexeme = yytext();
        return idF;
    } else if(estado == 3) {
        c.linea = yyline;
        lexeme = yytext();
        return idC;
    } else if(estado == 4) {
        c.linea = yyline;
        lexeme = yytext();
        return idS;
    } else if(estado == 5) {
        c.linea = yyline;
        lexeme = yytext();
        return idClass;
    } else if(estado == 6) {
        c.linea = yyline;
        lexeme = yytext();
        return idMet;
    } else {
        lexeme = yytext();
        return id;
    }
}
<YYINITIAL> {D} {c.linea = yyline; lexeme = yytext(); return num;}
<YYINITIAL> {CAR} {c.linea = yyline; lexeme = yytext(); return litcar;}
<YYINITIAL> {CA} {c.linea = yyline; lexeme = yytext(); return litcad;}
. {c.linea = yyline; lexeme = yytext(); return Error;}