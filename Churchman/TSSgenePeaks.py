HELP_STRING = """
TSSgenePeaks.py
Takes in a track file and determines where spikes lie. Peaks are called for every
gene where the mean reads per bp are above the cutoff value. 
For every position along a gene, the mean and standard deviation is determined
for a window of size <window> surrounding that position.
The position is a pause if its value is above <peakmin> and is <fold> standard
deviations above the mean. Also the mean for the window has to be above tnorm_thresh.

The process is repeated <rounds> times. After each iteration, the peaks are
removed from the gene and ignored for mean and standard deviation calculations.
New peaks are then found.


Author: Stirling Churchman
Date: Decemeber 29, 2009
Updated: May 4, 2011

* all options overided in script! Edit script to change these.
     -h     print this help message
     -f     data file
     -s     peak strength min
     -S     peak strength max
     -c     gene levels cutoff
     -o     output file (required)
"""
 
import sys

from getopt import getopt
import analysis as a
import numpy as np
import os


def main(argv=None):
    if argv is None:
        argv = sys.argv

    try:
        optlist, args = getopt(argv[1:], "hf:s:S:c:o:")
    except:
        print ""
        print HELP_STRING
        sys.exit(1)
       
   
       
    for (opt, opt_arg) in optlist:
        #print opt
        #print opt_arg
        if opt == '-h':
            print ""
            print HELP_STRING
            sys.exit(1)
        elif opt == '-f':
            condition = opt_arg
        elif opt == '-s':
            peakStrength = int(opt_arg)
        elif opt == '-S':
            peakStrengthMax = int(opt_arg)
        elif opt == '-c':
            cutoff= int(opt_arg)
        elif opt == '-o':
            outputFile = opt_arg
    
         
    
    
    condition = '/Users/stirls/lib/data/100917/Alignment/100919/t_IP_SET1'
    inputFile3 = '/Users/stirls/lib/TSS_data/TSS.txt'
    genomeFile = '/Users/stirls/lib/bowtie-0.12.0/genomes/sc_sgd_gff_20091011.fna'
    outputBase = '/Users/stirls/lib/analysis/pausing/data/'
    
    seqLength=15
    cutoff=0
    peakStrengthMin=0
    window=200
    fold=3
    peakmin=2
    tnorm_thresh=2
    rounds=3
    dir = 'cutoff_%s_peakStrengthMin_%s_means_wo_zeros0311' %(cutoff,peakStrengthMin)
    
    if dir not in os.listdir(outputBase):
        os.mkdir(outputBase+dir)
    print condition
    print window,fold,peakmin,tnorm_thresh,rounds
    
    peakSeq,peakPos =a.getPeakSeqs(genomeFile,condition+'_plus.txt',condition+'_minus.txt',condition+'_index.txt',seqLength,cutoff,peakStrengthMin,window,fold,peakmin,tnorm_thresh,rounds)
    filenames=condition.split('/')
    oFile = outputBase+dir+'/'+filenames[-4]+filenames[-1]+'_%s_%s_%s_%s_%s.csv' % (window,fold,peakmin,tnorm_thresh,rounds)
    outFile = open(oFile, 'w')
    
    upstream=[seq[0] for seq in peakSeq]
    downstream=[seq[1] for seq in peakSeq]
   
    percentA = float(len([seq for seq in upstream if seq[-1]=='A']))/len(upstream)*100
    percentT = float(len([seq for seq in downstream if seq[0]=='T']))/len(upstream)*100
    percentATC = float(len([seq for (i,seq) in enumerate(upstream) if (seq[-1]=='A' and downstream[i][:2]=='TC') ]))/len(upstream)*100
    print "Percent A is %s" % percentA
    print "Percent T is %s" % percentT
    print "Percent ATC is %s" % percentATC
    print "Number of pauses analyzed: %s" % len(upstream)
    
    
    for i,useq in enumerate(upstream):
        
        full_seq = useq+downstream[i]
        
        outFile.write(">%s_%s_%s_%s_%s_%s\n" % (peakPos[i][0],peakPos[i][1],peakPos[i][-4],peakPos[i][-3],peakPos[i][-2],peakPos[i][-1]))
        outFile.write("%s\n" % full_seq)
        
    
    outFile.close()
    
    
    


##############################################
if __name__ == "__main__":
    sys.exit(main())
