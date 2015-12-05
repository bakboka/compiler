begin 
   if not b>=1 or not a /=1 and a>=2 then print(b) fi;
   a:=a+1+2;
  b:=b+1;
  b:=a---b;
  while a/=1 do read(a) od; 
  if not b>=1 and not a /=1 or a>=2 then print(b) fi;
   if b/=1 then print(b) else if a/=1 then read(a) fi fi;
  if b/=1 then if a/=1 then read(a) else read(b) fi fi; 
  for a from 10 by 10 to 100 do print(a) od
end
