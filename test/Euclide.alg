
co Euclid's algorithm co

begin 
  read(a) ;
  read(b) ;
  while b /= 0 do
    c := b ; 
    while a >= b do     co computation of modulo co
      a := a-b
    od ;
    b := a ;
    a := c
  od ; 
  print(a) 
end
