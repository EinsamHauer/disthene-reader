grammar Graphite;

expression:
                call                    # ExpressionCall
            |   pathExpression          # ExpressionPathExpression
;

pathExpression: (pathElement DOT)* pathElement;

pathElement: partialPathElement+;

args: (WS* arg WS* COMMA WS*)* arg WS*;

arg:
                Boolean                 # ArgBoolean
            |   number                  # ArgNumber
            |   QoutedString            # ArgString
            |   expression              # ArgExpression
;

call: FunctionName LEFT_PAREN args RIGHT_PAREN;

partialPathElement: (EscapedChar)+ | FunctionName | path;

number: Integer | Float | Scientific;

path: (ValidChars | number)+;

fake: ValidChars;

Boolean: TRUE | FALSE;
Scientific: (Integer | Float) ('e' | 'E') Integer;
QoutedString: DoubleQuotedString | SingleQuotedString;
DoubleQuotedString: '"' ~[\r\n]*? '"';
SingleQuotedString: '\'' ~[\r\n]*? '\'';
FunctionName: [a-zA-Z_]+ [a-zA-Z_0-9]*;
ValidChars: [!#$%&*+\-/0123456789,:;<>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ\[\]\{\}^_`abcdefghijklmnopqrstuvwxyz|~]+;
Integer: '-'* DIGIT+;
Float: '-'* DIGIT+ '.' DIGIT+;
EscapedChar: BACKSLASH SYMBOL;

DOT: '.';
COMMA: ',';
LEFT_PAREN: '(';
RIGHT_PAREN: ')';
SYMBOL: [(){},=.'"];
BACKSLASH: '\\';
DIGIT: [0-9];
WS: (' ' | '\t')+ -> skip;
TRUE: ('t' | 'T')('r' | 'R')('u' | 'U')('e' | 'E');
FALSE: ('f' | 'F')('a' | 'A')('l' | 'L')('s' | 'S')('e' | 'E');


