A = 1;
w0 = 10*pi;
t = 0:.001:1
rho = 0.5;
sq = A*square(w0*t+rho); 
plot(t,sq);