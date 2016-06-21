grammar Graphite;

expression:
                call                    # ExpressionCall
            |   pathExpression          # ExpressionPathExpression
;

pathExpression: (pathElement DOT)* pathElement;

pathElement: (partialPathElement | matchEnum)+;

matchEnum: LEFT_BRACE (partialPathElement COMMA)* partialPathElement RIGHT_BRACE;

args: (WS* arg WS* COMMA WS*)* arg WS*;

arg:
                Boolean                 # ArgBoolean
            |   number                  # ArgNumber
            |   QoutedString            # ArgString
            |   expression              # ArgExpression
;

call: FunctionName LEFT_PAREN args RIGHT_PAREN;

partialPathElement: (EscapedChar)+ | FunctionName | Boolean | path;

number: Integer | Float | Scientific;

path: (ValidChars | number)+;

Boolean: TRUE | FALSE;
Integer: '-'* DIGIT+;
Float: '-'* DIGIT+ '.' DIGIT+;
Scientific: (Integer | Float) ('e' | 'E') Integer;
QoutedString: DoubleQuotedString | SingleQuotedString;
DoubleQuotedString: '"' ~[\r\n]*? '"';
SingleQuotedString: '\'' ~[\r\n]*? '\'';
FunctionName: [a-zA-Z_]+ [a-zA-Z_0-9]*;
ValidChars: [=!#$%&*+\-/0123456789:;<>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ\[\]^_`abcdefghijklmnopqrstuvwxyz|~]+;
EscapedChar: BACKSLASH SYMBOL;

DOT: '.';
COMMA: ',';
LEFT_BRACE: '{';
RIGHT_BRACE: '}';
LEFT_PAREN: '(';
RIGHT_PAREN: ')';
SYMBOL: [(){},=.'"];
BACKSLASH: '\\';
DIGIT: [0-9];
WS: (' ' | '\t')+ -> skip;
VALID_METRIC_CHAR: [!#$%&*+\-/0123456789,:;<>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ\[\]\{\}^_`abcdefghijklmnopqrstuvwxyz|~];
TRUE: ('t' | 'T')('r' | 'R')('u' | 'U')('e' | 'E');
FALSE: ('f' | 'F')('a' | 'A')('l' | 'L')('s' | 'S')('e' | 'E');


