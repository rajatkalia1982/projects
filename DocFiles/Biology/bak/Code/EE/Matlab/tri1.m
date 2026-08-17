A = 1;
w0 = 10*pi;
t = 0:.001:1
W = 0.5;
tri = A*sawtooth(w0*t+W); 
plot(t,tri);