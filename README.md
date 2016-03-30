# antenna_simu
假设一旦Medium将数据传输完毕,则所有的station都能感知到Medium已经idle了.

# 实现
所有的station注册Medium的observer,一旦Medium繁忙或者idle或者冲突了,通知所有的station
由ClockController进行对时间流动的控制
# 参考
http://www.cs.tut.fi/kurssit/TLT-6556/Slides/Lecture2.pdf