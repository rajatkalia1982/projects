import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
//import java.net.*;

public class Puyopuyo extends Applet implements Runnable, KeyListener{
//drawing vars
BufferedImage bufferdImg;
Graphics2D bufferdImgSurface;
Thread gameThread;
public int scrWD,scrHT;	//screen wd nd ht
Image eyecon[]=new Image[6];		//image of icon 0=blank,1=yellow,2=green,3blue,4=red,5=gray
public int bordPX,bordPY;
public int bordTop=50,bordLft=10;

//board vars
public int bordXct=6,bordYct=12;			//board dimensions	//use only even numbers for bordXct....
public int bordct=bordXct*bordYct;			//number of icons
public int bordX[]=new int[bordct];			//bord x			
public int bordY[]=new int[bordct];			//bord y
public int bordpxlX[]=new int[bordct];		//xpxel position 
public int bordpxlY[]=new int[bordct];		//ypixel position
public int bordicon[]=new int[bordct];		//color of icon 0=blank,1=yellow,2=green,3blue,4=red,5=gray
public int bordLnk[]=new int[bordct];		//link number	*determines their linkage to surroounding icons
public boolean bordpvt[]=new boolean[bordct];	//is capsule pivot	
public boolean bordcapsule[]=new boolean[bordct];	//is capsule
public boolean bordfxd[]=new boolean[bordct];		//is fixed to bord?
public int Lnkct;									//link count

public int iconz=32;		//icon size
public boolean canCtrlCaps;	//can you control the capsule?
public int capsDxn;			//capsule direction
public boolean isderCaps;
public int cap1,cap2;

/*
capsDxn---
[pvt][odr]	=	3

[odr][pvt]	=	1

[pvt]
[odr]		=	4

[odr]
[pvt]		=	2

* the pvt being the Front of the capsule

1	=	going left
2	=	down
3	=	ryt
4	=	up


the number corresponds to these :
and the number inside the plane corresponds to the 
index of array

			12	|	66	67	68	69	70	71
			11	|	60	61	62	63	64	65
			10	|	54	55	56	57	58	59
			9	|	48	49	50	51	52	53
			8	|	42	43	44	45	46	47
			7	|	36	37	38	39	40	41
	bord Y	6	|	30	31	32	33	34	35
			5	|	24	25	26	27	28	29
			4	|	18	19	20	21	22	23
			3	|	12	13	14	15	16	17
			2	|	6	7	8	9	10	11
			1	|  _0_ _1_ _2_ _3_ _4_ _5_
					1	2	3	4	5	6
						bord X			
			
*/


//game timing vars
public long dwnTymHoldr;		//holder for the down tym
public long dwnTym;		//lenght of delay for icons to fall
public long dwnTymStrt;				//start of down time
public long movedelay;				//delay time
public long movedelayStrt;			//start of move(not down) delay
public boolean ismovedelay;			//are you delaying the move?
public boolean freeze;
public long freezeStrt,freezeTym;

//game switches
boolean isOver;		//game over
boolean isAuto;		//the game moves by itself
boolean isStart;	//start to play?
boolean isPlay;		//r we playing?

//move swithces
boolean toLft;	//move capsule to left?
boolean toRyt;	//move capsule to ryt?
boolean rot8L;	//rot8 ccwise?
boolean rot8R;	//rot8 cwise?
boolean toDwn;	//move icons/capsule down?
boolean toSwc;	//switch capsule places?

//score vars
public long hiscore,score;
public int lvl;
public long lnkCtr;

//sleep vars		i just copied this part from a tutorial but i know (barely) how it works
//					it's self explanatory :)
long tick_start,	// Frame start time
tick_end,			// End frame time
tick_duration,		// Time taken to display the frame
sleep_duration;		// How long to sleep for
static final int MIN_SLEEP_TIME = 1,	// Min time to sleep
MAX_FPS = 50,							// Max frame rate.
MAX_MS_PER_FRAME = 1000 / MAX_FPS;		// MS per frame


//xtravars
public int gg;

public void start(){		//i just copied this part from a tutorial :)
	Thread gameThread = new Thread(this);
	gameThread.start();
	}

public void init(){	//i also copid this :) this is where initialization takes place
	int i;
	if (bordXct<6)			// min size of vordxct =6
		bordXct=6;
	else if (bordXct%2!=0)	//or odd number
		bordXct=bordXct+1;
	if (bordYct<12)			//min size of bordyct =12
		bordYct=12;
	initBoard();	//initialize the boards
	bufferdImg = (BufferedImage)createImage(scrWD,scrHT);
	bufferdImgSurface = bufferdImg.createGraphics();
	isOver=false;	//initialize values...
	toLft=false;
	toRyt=false;
	rot8L=false;
	rot8R=false;
	toDwn=false;
	toSwc=false;
	isPlay=false;
	isStart=true;
	isderCaps=false;
	hiscore=0;
	for (i=1;i<=5;i++)		// initialize the images
		eyecon[i]=getImage(getCodeBase(), "img/"+i+".png");
	addKeyListener(this);
	}

public void run(){
	while(true){		// Starts the loop
		tick_start = System.currentTimeMillis();
		if (ismovedelay && System.currentTimeMillis()>movedelayStrt+movedelay)	//if current time > delaystrt and delay
			ismovedelay=false;	//u can now make a move :)
		if (System.currentTimeMillis()>dwnTym+dwnTymStrt && isPlay) //if current time > dwntymstart + downtym
			toDwn=true;	//move icons down
		if (freeze)
			if (System.currentTimeMillis()>freezeTym+freezeStrt){
				dwnTymStrt=System.currentTimeMillis();
				freeze=false;				
				}
		repaint(); // Redraw the screen
		tick_end = System.currentTimeMillis();
		tick_duration = tick_end - tick_start;
		sleep_duration = MAX_MS_PER_FRAME - tick_duration;
		if (sleep_duration < MIN_SLEEP_TIME)
			sleep_duration = MIN_SLEEP_TIME;
		try {
			Thread.sleep(sleep_duration);
			}
		catch(InterruptedException e){}
		}
	}

public void paint(Graphics g){
	update(g);
	}

public void update(Graphics g){	//updates the screen
	if (freeze)	//if is over, no mre...
		return;
	Graphics2D g2 = (Graphics2D)g;
	int i;
	boolean fin=false,tobreak=false;
	bufferdImgSurface.clearRect(0, 0, scrWD, scrHT);
	bufferdImgSurface.setBackground(Color.black);
	drwBorder(bufferdImgSurface);	//draws the lines
	drwscores(bufferdImgSurface);
	if (isStart){
		bufferdImgSurface.setColor(Color.yellow);
		bufferdImgSurface.drawString("Hit Enter to Start..!",(scrWD/2)-50,scrHT/2);
		}
	if (isOver){
		bufferdImgSurface.setColor(Color.yellow);
		bufferdImgSurface.drawString("Game Over..!",(scrWD/2)-35,bordTop-10);	
		//drwCap(bufferdImgSurface);
		drwImg(bufferdImgSurface);	//drwas the entire bord
		}
	
	if (isPlay){
		if (isAuto){	//the game automatically controls the game
			dwnTym=50;	//makes down tym a bit faster
			if(chkFloat()){	//there are floating icons
				if(toDwn){	//can move icons down?
					toDwn=false;
					moveallDwn();	//move all floating icons down
					dwnTymStrt=System.currentTimeMillis();	//set dwn tym start
					}
				}
			else{			//no floating
				lnkBord();	//link the boed
				if (Lnkct==0){	//there are no links
					isAuto=false;	//return to player control
					toDwn=false;	
					dwnTym=dwnTymHoldr;	//dwntym is reset to initialized value
					dwnTymStrt=System.currentTimeMillis();	//set dwntym start
					}
				else{		//there is a link 
					for (i=1;i<=Lnkct;i++){	//loop all links check every link
						if (chkbordlnk(i)){	// if there is something to break
							tobreak=true;	//there is
							break;
							}
						}
					if (!tobreak){		//no links to breaks
						isAuto=false;	//return to player control (same as when lnkCt==0
						toDwn=false;
						dwnTym=dwnTymHoldr;
						dwnTymStrt=System.currentTimeMillis();					
						}
					else{	//break those links :) and make the others float
						for (i=1;i<=Lnkct;i++){	//loop all link
							if (chkbordlnk(i)){	//this link number rulzzz
								score=score+getscore(i);	//add to score
								breaklnk(i);	//break this link
								}	
							}
						lnkCtr++;
						makeFloat();	//makes the PROBABLE icons float(unfixed to bord)
						chkstage();
						toDwn=false;
						freeze=true;
						freezeStrt=System.currentTimeMillis();
						freezeTym=250;
						}
					}
				}
			}
		else{	//player control
			if (!isderCaps)
				newCaps();
			if (!chkFloat()){	//if there are no floating
				if (!transCaps()){	//create new capsule, if false end of game
					ismovedelay=true;	
					toDwn=false;
					fin=true;
					}
				else	//true, u can control the capsule
					canCtrlCaps=true;
				}
			if(!ismovedelay){	//is your move delayed?
				if (toLft){		//move caps to left?
					toLft=false;
					moveCLft();	//move it left
					}
				else if(toRyt){	//to ryt?
					toRyt=false;
					moveCRyt();	//go ryt
					}
				else if(toSwc){	//switch?
					toSwc=false;
					switchCaps();	//switch places
					}
				else if(rot8R){		//rot8 ryt?
					rot8R=false;
					moverot8R();	//then go on
					}
				else if(rot8L){		//or left
					rot8L=false;
					moverot8L();	//my pleasure
					}
				}
			if(toDwn){	//move the capsule down?
				toDwn=false;
				movecapDwn();	//move capsule down
				dwnTymStrt=System.currentTimeMillis();	
				}
			}
			drwCap(bufferdImgSurface);
			drwImg(bufferdImgSurface);	//drwas the entire bord
		}
		
	if (fin){	//game over?
		//for (i=0;i<bordct;i++)	//loop all icon
		//	if (bordicon[i]!=0)	//if not blank
		//		bordicon[i]=5;	//make it gray
		isOver=true;	//is over... :(
		isPlay=false;
		}
	bufferdImgSurface.setColor(Color.yellow);
	bufferdImgSurface.drawString("Gawang Pinoy...!",5,scrHT-15);
	bufferdImgSurface.drawString("Gawa ni Jecson Eslabra...!",5,scrHT-5);
	//xtraxtra
	//bufferdImgSurface.drawString("pvt bx,by = "+String.valueOf(bordX[getIdxpvt()])+","+String.valueOf(bordY[getIdxpvt()]),10,20);
	//bufferdImgSurface.drawString("idxpvt "+String.valueOf(getIdxpvt()), 10,30);
	//bufferdImgSurface.drawString("odr bx,by = "+String.valueOf(bordX[getIdxodr()])+","+String.valueOf(bordY[getIdxodr()]),10,20);
	//bufferdImgSurface.drawString("idxodr "+String.valueOf(getIdxodr()), 10,30);
	//bufferdImgSurface.drawString(String.valueOf(System.currentTimeMillis()), 10,scrHT-30);
	//bufferdImgSurface.drawString(String.valueOf(dwnTymStrt), 10,scrHT-40);
	g2.drawImage(bufferdImg, 0, 0, this);
	}

public void initBoard(){	//init the bord
	int i;
	canCtrlCaps=false;
	capsDxn=0;
	bordPX=iconz*bordXct;
	bordPY=iconz*bordYct;
	scrWD=(bordLft*2)+bordPX;
	scrHT=bordTop+bordPY+bordLft+25;
	int x=1,y=1;
	for (i=0;i<bordct;i++){	//loop all bordicons
		bordX[i]=x;	//set bordx
		bordY[i]=y;	//set bordy
		bordpxlX[i]=((x-1)*iconz)+bordLft;	//set x pixel
		bordpxlY[i]=(bordTop+bordPY)-(y*iconz);		//set y pixel
		bordicon[i]=0;					//icon=blank
		bordpvt[i]=false;
		bordcapsule[i]=false;
		bordfxd[i]=false;	//unfix all icons
		x++;	//add x
		if (x>bordXct){	//x>width of bord?
			x=1;	//reset to 1
			y++;	//add y
			}
		}
	clrLnk();	//clear all loinks
	}

public void initGame(){
	initBoard();
	score=0;
	lvl=1;
	lnkCtr=0;
	dwnTymHoldr=2500;
	dwnTym=dwnTymHoldr;
	ismovedelay=false;
	dwnTymStrt=System.currentTimeMillis();	//set dwn tym start
	}

public void chkstage(){
	if (lnkCtr>=5){
		lvl++;
		if (dwnTymHoldr>500)
			dwnTymHoldr=dwnTymHoldr-500;
		else if (dwnTymHoldr>100)
			dwnTymHoldr=dwnTymHoldr-100;
		dwnTym=dwnTymHoldr;
		lnkCtr=0;
		}
	}
	
public int getscore(int ini){
	int i;
	int ctr=0;
	for (i=0;i<bordct;i++){
		if (bordLnk[i]==ini)	//if linknumber of icon==sought-after linknumber
			ctr++;
		}
	return 200+((ctr-4)*100);
	}
	
public void drwscores(Graphics2D sorpes){
	sorpes.setColor(Color.white);
	if (score>hiscore && score>5000)
		hiscore=score;
	sorpes.drawString("HI-Score  : " + hiscore,10,10);
	sorpes.drawString("Score       : " + score,10,20);
	if (lvl!=0)
		sorpes.drawString("Lvl.  : " + lvl,10,bordTop-7);
	}

public void drwCap(Graphics2D sorpes){
	sorpes.drawImage(eyecon[cap1],(scrWD/2)+iconz,bordTop-iconz-5,this);
	sorpes.drawImage(eyecon[cap2],(scrWD/2)+(iconz*2),bordTop-iconz-5,this);
	}
	
public void drwImg(Graphics2D sorpes){	//drwas the icons
	int i;
	int kulay;
	for(i=0;i<bordct;i++){	//loop all icons
		kulay=bordicon[i];	//get icon color
		if (kulay!=0){	//if not blank
			sorpes.drawImage(eyecon[kulay],bordpxlX[i],bordpxlY[i],this);
//			if (kulay==1)
//				sorpes.setColor(Color.yellow);
//			else if (kulay==2)
//				sorpes.setColor(Color.green);
//			else if (kulay==3)
//				sorpes.setColor(Color.blue);
//			else if (kulay==4)
//				sorpes.setColor(Color.red);
//			else
//				sorpes.setColor(Color.gray);
			//set according to color
//			sorpes.fillOval(bordpxlX[i],bordpxlY[i],iconz,iconz);	//dra bid circle
//			sorpes.setColor(Color.black);
//			sorpes.fillOval(bordpxlX[i]+7,bordpxlY[i]+8,6,7);	//left eye
//			sorpes.fillOval(bordpxlX[i]+18,bordpxlY[i]+8,6,7);	//ryt eye
//			sorpes.fillOval(bordpxlX[i]+5,bordpxlY[i]+18,20,7);	//smile :)
			}
		}
	}

public void moveCLft(){
	int c1,c2,cx1,cy1,cx2,cy2;
	c1=getIdxpvt();	//get the index if the PVT of capsule
	c2=getIdxodr();	//get the index of the ODR of capsule
	cx1=bordX[c1];	//self explanatory
	cy1=bordY[c1];
	cx2=bordX[c2];
	cy2=bordY[c2];	
	if (cx2==1 || cx1==1)	//if any icon on the left border of bord ucant move enimore
		return;
	if (cx1>cx2 && !bordfxd[c2-1]){		//caps=horizontal .. the PVT is on the ryt && nothing on left
		bordicon[c2-1]=bordicon[c2];	//copy ODR icon to left
		bordicon[c2]=bordicon[c1];		//copy PVT icon to left
		bordicon[c1]=0;					//make current PVT icon blank
		bordpvt[c2]=true;				//makes ODR the PVT
		bordcapsule[c2-1]=true;			//makes left of ODR capsule
		bordpvt[c1]=false;				//unpivot the PVT
		bordcapsule[c1]=false;			//uncapsule the PVT
		}
	else if(cx1<cx2 && !bordfxd[c1-1]){	//caps=horizontal .. the pvot is on the lft && nothing on left
		bordicon[c1-1]=bordicon[c1];	//same as above :)
		bordicon[c1]=bordicon[c2];
		bordicon[c2]=0;
		bordpvt[c1-1]=true;
		bordcapsule[c1-1]=true;
		bordpvt[c1]=false;
		bordcapsule[c2]=false;
		}
	else if (!bordfxd[c1-1] && !bordfxd[c2-1]){	//caps=vertical .. left of both capsule is unfxd
		bordicon[c1-1]=bordicon[c1];	//same also as above:)
		bordicon[c2-1]=bordicon[c2];
		bordicon[c2]=0;
		bordicon[c1]=0;
		bordcapsule[c1-1]=true;
		bordcapsule[c2-1]=true;
		bordcapsule[c1]=false;
		bordcapsule[c2]=false;
		bordpvt[c1-1]=true;
		bordpvt[c1]=false;
		}
	}	

/*
the same concept of moving applies when going ryt
*/

public void moveCRyt(){
	int c1,c2,cx1,cy1,cx2,cy2;
	c1=getIdxpvt();
	c2=getIdxodr();
	cx1=bordX[c1];
	cy1=bordY[c1];
	cx2=bordX[c2];
	cy2=bordY[c2];	
	if (cx1==bordXct || cx2==bordXct)
		return;
	if (cx1>cx2 && !bordfxd[c1+1]){		//caps=horizontal .. the pvot is on the ryt && nothing on ryt
		bordicon[c1+1]=bordicon[c1];
		bordicon[c1]=bordicon[c2];
		bordicon[c2]=0;
		bordpvt[c1+1]=true;
		bordcapsule[c1+1]=true;
		bordpvt[c1]=false;
		bordcapsule[c2]=false;
		}
	else if(cx1<cx2 && !bordfxd[c2+1]){	//caps=horizontal .. the pvot is on the lft && nothing on ryt
		bordicon[c2+1]=bordicon[c2];
		bordicon[c2]=bordicon[c1];
		bordicon[c1]=0;
		bordpvt[c2]=true;
		bordcapsule[c2+1]=true;
		bordpvt[c1]=false;
		bordcapsule[c1]=false;
		}
	else if (!bordfxd[c1+1] && !bordfxd[c2+1]){	//caps=vertical .. and right of bot capsule unfxd
		bordicon[c1+1]=bordicon[c1];
		bordicon[c2+1]=bordicon[c2];
		bordicon[c2]=0;
		bordicon[c1]=0;
		bordcapsule[c1+1]=true;
		bordcapsule[c2+1]=true;
		bordcapsule[c1]=false;
		bordcapsule[c2]=false;
		bordpvt[c1+1]=true;
		bordpvt[c1]=false;
		}
	}

public void switchCaps(){	//switch capsule color
	int c1,c2,cx1,cy1,cx2,cy2,tmp;
	c1=getIdxpvt();	//already discussed
	c2=getIdxodr();
	cx1=bordX[c1];
	cy1=bordY[c1];
	cx2=bordX[c2];
	cy2=bordY[c2];
	tmp=bordicon[c1];	//hold PVT icon color
	bordicon[c1]=bordicon[c2];	//Copy ODR icon color to PVT
	bordicon[c2]=tmp;	//copy held to ODR
	}

public void moverot8R(){	//rotate the capsule clockwise
	int c1,c2,cx1,cy1;
	c1=getIdxpvt();	//:)
	c2=getIdxodr();
	cx1=bordX[c1];
	cy1=bordY[c1];

	if (capsDxn==1){	//ryt going down
		if (cy1!=bordYct){	//on topmost row?
			bordicon[c1+bordXct]=bordicon[c2];	//copy ODR to target ODR
			bordcapsule[c1+bordXct]=true;		//make it capsule
			bordicon[c2]=0;						//un-icon currect ODR
			bordcapsule[c2]=false;				//un-capsule current ODR
			capsDxn=2;							//change dxn
			}
		}
	else if (capsDxn==2){	//down going left
		if (cx1!=bordXct && !bordfxd[c1+1]){	//if not on ryt border of bord && tgt ODR is not fxd
			bordicon[c1+1]=bordicon[c2];
			bordcapsule[c1+1]=true;
			bordicon[c2]=0;
			bordcapsule[c2]=false;
			capsDxn=3;
			}
		}
	else if (capsDxn==3){	//left going up
		if (!chkUnder(cx1,cy1)){	//if nothing under PVT
			bordicon[c1-bordXct]=bordicon[c2];
			bordcapsule[c1-bordXct]=true;
			bordicon[c2]=0;
			bordcapsule[c2]=false;
			capsDxn=4;
			}
		}
	else if (capsDxn==4){	//up gpoing left
		if (cx1!=1 && !bordfxd[c1-1]){	//if not on left border of bord && tgt ODR unfxd
			bordicon[c1-1]=bordicon[c2];
			bordcapsule[c1-1]=true;
			bordicon[c2]=0;
			bordcapsule[c2]=false;
			capsDxn=1;
			}
		}
	}

/*
same concept of rotating as moverot8R
*/
public void moverot8L(){	//rotate counter clockwise
	int c1,c2,cx1,cy1;
	c1=getIdxpvt();
	c2=getIdxodr();
	cx1=bordX[c1];
	cy1=bordY[c1];

	if (capsDxn==3){	//left going down
		if (cy1!=bordYct){
			bordicon[c1+bordXct]=bordicon[c2];
			bordcapsule[c1+bordXct]=true;
			bordicon[c2]=0;
			bordcapsule[c2]=false;
			capsDxn=2;
			}
		}
	else if (capsDxn==4){	//up going left
		if (cx1!=bordXct && !bordfxd[c1+1]){
			bordicon[c1+1]=bordicon[c2];
			bordcapsule[c1+1]=true;
			bordicon[c2]=0;
			bordcapsule[c2]=false;
			capsDxn=3;
			}
		}
	else if (capsDxn==1){	//ryt going up
		if (!chkUnder(cx1,cy1)){
			bordicon[c1-bordXct]=bordicon[c2];
			bordcapsule[c1-bordXct]=true;
			bordicon[c2]=0;
			bordcapsule[c2]=false;
			capsDxn=4;
			}
		}
	else if (capsDxn==2){	//down going ryt
		if (cx1!=1 && !bordfxd[c1-1]){
			bordicon[c1-1]=bordicon[c2];
			bordcapsule[c1-1]=true;
			bordicon[c2]=0;
			bordcapsule[c2]=false;
			capsDxn=1;
			}
		}
	}

public void movecapDwn(){	//movecapsule down
	if (!canCtrlCaps)
		return;
	int c1,c2,cx1,cy1,cx2,cy2,tmp;
	boolean c1u,c2u;	//checkunder for PVT,ODR
	c1=getIdxpvt();	//:)
	c2=getIdxodr();
	cx1=bordX[c1];
	cy1=bordY[c1];
	cx2=bordX[c2];
	cy2=bordY[c2];
	if (capsDxn%2==0){	//verticalcapsulse
		if (cy2<cy1){	//ODR under PVT
			if (chkUnder(cx2,cy2)){	//there's something beneath mek it fxd
				bordfxd[c2]=true;	//ODR fxd
				bordfxd[c1]=true;	//PVT fxd
				bordpvt[c1]=false;	//un-pvt
				bordcapsule[c1]=false;	//uncaps PVT
				bordcapsule[c2]=false;	//uncaps ODR
				//lnkBord();
				canCtrlCaps=false;	//cant control capsule
				isAuto=true;	//set to automatic
				}
			else{	//nothing just move down
				moveiconDwn(cx2,cy2);	//move ODR down
				moveiconDwn(cx1,cy1);	//move PVT down
				bordpvt[c2]=true;		//make current ODR PVT
				bordpvt[c1]=false;		//un-pvot PVT
				bordcapsule[c2-bordXct]=true;	//make 
				bordcapsule[c1]=false;
				}
			}
		else{	//PVT under ODR
			if (chkUnder(cx1,cy1)){	//there's something beneath make it fixd
				bordfxd[c2]=true;
				bordfxd[c1]=true;
				bordpvt[c1]=false;
				bordcapsule[c1]=false;
				bordcapsule[c2]=false;
				//lnkBord();
				canCtrlCaps=false;
				isAuto=true;
				}
			else{	//nothing just move down
				moveiconDwn(cx1,cy1);
				moveiconDwn(cx2,cy2);
				bordpvt[c1-bordXct]=true;
				bordpvt[c1]=false;
				bordcapsule[c1-bordXct]=true;
				bordcapsule[c2]=false;
				}
			}
		}
	else{		//horizontal capsule
		c1u=chkUnder(cx1,cy1);
		c2u=chkUnder(cx2,cy2);
		if (c1u || c2u){	//if eni under of the caps is fixed
			if (c1u)	//if under PVT
				bordfxd[c1]=true;	//make it fixed
			if (c2u)
				bordfxd[c2]=true;
			bordpvt[c1]=false;
			bordcapsule[c1]=false;
			bordcapsule[c2]=false;
			//lnkBord();
			canCtrlCaps=false;
			isAuto=true;
			}
		else{	//nothing just move them down
			moveiconDwn(cx1,cy1);
			moveiconDwn(cx2,cy2);
			bordcapsule[c1]=false;
			bordcapsule[c2]=false;
			bordcapsule[c1-bordXct]=true;
			bordcapsule[c2-bordXct]=true;
			bordpvt[c1]=false;
			bordpvt[c1-bordXct]=true;
			}
		}
	}

public void clrLnk(){	//removes all link
	int i;
	Lnkct=0;
	for (i=0;i<bordct;i++)
		bordLnk[i]=0;	//set link number to 0
	}

public boolean chkbordlnk(int inLnknum){	//check if inLnknum is linked >4 .. inLnknum is a linknumber
	int i;
	int ctr=0;
	for (i=0;i<bordct;i++){
		if (bordLnk[i]==inLnknum)	//if linknumber of icon==sought-after linknumber
			ctr++;
		if (ctr==4)	//:)
			return true;	//this is a linknumber with >=4 icons linked
		}
	return false;	//nope
	}

public void breaklnk(int inLnk){	//breaks the link of the linknumber used in conjuction with chkbordLnk
	int i;
	for (i=0;i<bordct;i++){
		if (bordLnk[i]==inLnk){	//same link num?
			bordicon[i]=0;		//make it blank
			bordfxd[i]=false;	//make it unfxd
			}
		}
	}

public void lnkBord(){	//link the bord
	clrLnk();	//clear links
	int x,y,idx,idxtgt;
	for (x=1;x<=bordXct;x++){		//loop to ryt
		for (y=1;y<=bordYct;y++){	//loop to up
			idx=getbordIdx(x,y);	//get index of the current (x,y)
			if (bordfxd[idx]){	//if this is fixd
				if (y!=bordYct){		//not yet on top u can link upwards
					idxtgt=getbordIdx(x,y+1);	//get idx of target icon
					if (bordicon[idxtgt]==bordicon[idx] && bordicon[idx]!=0 && bordfxd[idxtgt]){	//this icaon is same as up && not balnk
						if (bordLnk[idxtgt]==0 && bordLnk[idx]==0){	//no links yet 
							Lnkct++;	//create new link
							bordLnk[idx]=Lnkct;	//and assign
							bordLnk[idxtgt]=Lnkct;	//to both
							}
						else{	//one of them or both have links
							if (bordLnk[idx]!=0 && bordLnk[idxtgt]==0)	//tgtidx have no link 
								bordLnk[idxtgt]=bordLnk[idx];
							else if	(bordLnk[idx]==0 && bordLnk[idxtgt]!=0)	//idx have no link 
								bordLnk[idx]=bordLnk[idxtgt];
							else{	//lnk not same make new link number
								Lnkct++;	//create new linknumber
								lnkchange(bordLnk[idxtgt],Lnkct);	//change the bordlnk of TGT to new lnkct
								lnkchange(bordLnk[idx],Lnkct);		//change the bordlnk of IDX to new lnkct
								}
							}
						}
					}
				if (x!=bordXct){	//not yet rytmost, link to ryt
					idxtgt=getbordIdx(x+1,y);
					if (bordicon[idxtgt]==bordicon[idx] && bordicon[idx]!=0 && bordfxd[idxtgt]){	//this icaon is same as up && not balnk
						if (bordLnk[idxtgt]==0 && bordLnk[idx]==0){	//uppertarget no links yet 
							Lnkct++;	//create new link
							bordLnk[idx]=Lnkct;
							bordLnk[idxtgt]=Lnkct;
							}
						else{	//upper tgt already linkd
							if (bordLnk[idx]!=0 && bordLnk[idxtgt]==0)	//tgtidx have no link 
								bordLnk[idxtgt]=bordLnk[idx];
							else if	(bordLnk[idx]==0 && bordLnk[idxtgt]!=0)	//idx have no link 
								bordLnk[idx]=bordLnk[idxtgt];
							else{	//lnk not same make new link number
								Lnkct++;
								lnkchange(bordLnk[idxtgt],Lnkct);
								lnkchange(bordLnk[idx],Lnkct);
								}
							}
						}
					}
				}
			
		//	else	//not fixd, floating or icon=0, no need to traverse, break to next x
		//		break;
			}
		}
	

	}

public void lnkchange(int lnkfrm,int lnkto){	//change the link number of a into b .. self explanatory :)
	int i;
	for (i=0;i<bordct;i++){
		if (bordLnk[i]==lnkfrm)
			bordLnk[i]=lnkto;
		}
	}

public void moveallDwn(){	//move all icons down
	int i;
	for (i=0;i<bordct;i++){
		if (!bordfxd[i] && bordicon[i]!=0){		//if not yet fixd and icon not blank
			if (chkUnder(bordX[i],bordY[i])){		//if there is something under, make it fixd
				bordfxd[i]=true;
				}
			else{		//theres nothing beneath, move this to there
				moveiconDwn(bordX[i],bordY[i]);
				}
			}
		}
	}

public void makeFloat(){	//chacks the highest pt of each columns and make the remaining float
	int x,y,z;
	boolean nomore;
	for (x=1;x<=bordXct;x++){	//loop bord width
		nomore=false;
		for (y=1;y<=bordYct;y++){	//loop bord ht
			z=getbordIdx(x,y);	//get index of (x,y)
			if (!bordfxd[z] && !nomore)	//if this ht unfxd, make all succeeding unfxd
				nomore=true;
			if (nomore)
				bordfxd[z]=false;
			}
		}
	}

public void newCaps(){	//generates a new capsule
	cap1=getRandIcon();
	cap2=getRandIcon();
	isderCaps=true;
	}

public boolean transCaps(){	//transfercaps to bord
	capsDxn=3;
	int x,y,z;
	z=bordXct/2;
	x=getbordIdx(z,bordYct);
	if (bordfxd[x])	//already fxd?
		return false;	//u cant create new capsule
	y=getbordIdx(z+1,bordYct);
	if (bordfxd[y])	
		return false;
		
	bordpvt[x]=true;	//make this PVT
	bordcapsule[x]=true;	//and caps
	bordicon[x]=cap1;	//get a random icon color
	bordcapsule[y]=true;	//make this caps
	bordicon[y]=cap2;
	isderCaps=false;
	return true;
	}


public int getIdxpvt(){	//gets the index of PVT
	int i;
	for (i=0;i<bordct;i++){
		if (bordpvt[i] && bordcapsule[i])	//if this is PVT and this is also caps
			break;
		}
	return i;
	}

public int getIdxodr(){	//get ODR index
	int i;
	for (i=0;i<bordct;i++){
		if (!bordpvt[i] && bordcapsule[i])	//this is not PVT "but" caps
			break;
		}
	return i;
	}

public int getRandIcon(){	//:)
	return (int)((Math.random()*100)%4)+1;
	}

public boolean chkFloat(){	//check if there are still floating
	int i;
	for (i=0;i<bordct;i++){
		if (bordicon[i]!=0 && !bordfxd[i])	//if not fxd but not blank
			return true;
		}
	return false;
	}

public int getbordIdx(int inX, int inY){	//returns the index of (x,y)
	return (((inY-1)*bordXct)+inX)-1;
	}

public boolean chkUnder(int inbordX,int inbordY){	//check if under of (x,y) is fixed or floor
	if (inbordY==1)	//on lowest row?
		return true;
	int lowbrd,bordidx;
	lowbrd=inbordY-1;	//get the under y
	bordidx=getbordIdx(inbordX,lowbrd);	//get the under idx
	if (bordfxd[bordidx])	//if it is fixed
		return true;
	return false;
	}

public void moveiconDwn(int inX, int inY){	//moves the icon of current (x,y) down
	int lowbrd,bordidx;
	lowbrd=inY-1;
	bordidx=getbordIdx(inX,lowbrd);
	bordicon[bordidx]=bordicon[getbordIdx(inX,inY)];
	bordicon[getbordIdx(inX,inY)]=0;
	}

public void drwBorder(Graphics2D sorpes){	//draws the lines
	//vertical
	drwLine(bordLft-3,bordTop-3,bordLft-3,bordTop+bordPY+3,sorpes);
	drwLine(bordLft+bordPX+3,bordTop-3,bordLft+bordPX+3,bordTop+bordPY+3,sorpes);
	//drwLine(7,37,7,403,sorpes);
	//drwLine(193,37,193,403,sorpes);
	//horisontal
	drwLine(bordLft-3,bordTop-3,(scrWD/2)-iconz,bordTop-3,sorpes);
	drwLine((scrWD/2)+iconz,bordTop-3,bordLft+bordPX+3,bordTop-3,sorpes);
	drwLine(bordLft-3,bordTop+bordPY+3,bordLft+bordPX+3,bordTop+bordPY+3,sorpes);
	//drwLine(7,37,70,37,sorpes);
	//drwLine(130,37,193,37,sorpes);
	//drwLine(7,403,193,403,sorpes);
	}

public void drwLine(int X,int Y,int x,int y,Graphics2D sorpes){ //print borderlines
	if (x==X){ //vertical line
		sorpes.setColor(Color.white);
		sorpes.drawLine(X,Y,x,y);
		sorpes.drawLine(X-1,Y+1,x-1,y-1);
		sorpes.drawLine(X-2,Y+2,x-2,y-2);		
		sorpes.setColor(Color.gray);
		sorpes.drawLine(X+1,Y+1,x+1,y-1);
		sorpes.drawLine(X+2,Y+2,x+2,y-2);
		}
	if (y==Y){ //horizontal
		sorpes.setColor(Color.white);
		sorpes.drawLine(X,Y,x,y);
		sorpes.drawLine(X+1,Y-1,x-1,y-1);
		sorpes.drawLine(X+2,Y-2,x-2,y-2);
		sorpes.setColor(Color.gray);
		sorpes.drawLine(X+1,Y+1,x-1,y+1);
		sorpes.drawLine(X+2,Y+2,x-2,y+2);
		}
	}

public void keyPressed(KeyEvent ke){
	int kcode=gg=ke.getKeyCode();	//get the key code
	if (!ismovedelay && canCtrlCaps && isPlay){	//if u can control the capsule and not move delay
		movedelayStrt=System.currentTimeMillis();
		if (kcode==100){	//numpad 4
			movedelay=50;
			toLft=true;		//moveleft
			}
		if (kcode==101){	//numpad 5
			dwnTym=50;		//movedown a bit faster
			}
		if (kcode==102){	//numpad 6
			movedelay=50;
			toRyt=true;		//moveryt
			}
		if (kcode==103){	//numpad 7
			movedelay=50;
			rot8L=true;		//rot8left
			}
		if (kcode==104){	//numpad 8
			movedelay=50;
			toSwc=true;		//switch places;
			}
		if (kcode==105){	//numpad 9
			movedelay=50;
			rot8R=true;		//rot8ryt
			}
		ismovedelay=true;	//delay the move
		return;
		}
	if (kcode==10){
		if (isOver && !isStart){
			isStart=true;
			isOver=false;
			}
		else if (isStart && !isPlay){
			initGame();
			isPlay=true;
			isStart=false;
			}
		}
	}
public void keyTyped(KeyEvent ke){}
public void keyReleased(KeyEvent ke){
	if (canCtrlCaps)	//if u can control caps
		dwnTym=dwnTymHoldr;	//reset the dwntym to held tym
	}
}