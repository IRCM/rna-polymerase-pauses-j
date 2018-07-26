HELP_STRING = """
TSS_index.py

Author: Stirling Churchman
Date: March 4, 2010
     -f     track file base to be indexed
     -t     TSS file to use
     -h     print this help message
     
"""
 
import sys

from getopt import getopt



def main(argv=None):
    if argv is None:
        argv = sys.argv
    trackFilename=''
    TSSFilename=''
    outFilename = ''
    
    try:
        optlist, args = getopt(argv[1:], "ht:f:")
    except:
        print ""
        print HELP_STRING
        sys.exit(1)
       
       
    for (opt, opt_arg) in optlist:
        if opt == '-h':
            print ""
            print HELP_STRING
            sys.exit(1)
        elif opt == '-f':
            trackFilename=opt_arg
        elif opt == '-t':
            TSSFilename = opt_arg
        
        
    if TSSFilename == "" or trackFilename == "":
        print HELP_STRING
        sys.exit(1)
        
#    TSSFile = open(TSSFilename, 'r')
    trackFile1 = open(trackFilename+'_plus.txt','r')
    trackFile2 = open(trackFilename+'_minus.txt','r')
    chr1=trackFile1.readline().replace('\n','').split('=')
    chr1=chr1[1]
    lineNum1=0
    chr2=trackFile2.readline().replace('\n','').split('=')
    chr2=chr2[1]
    lineNum2=0
    geneIndex={}
    start=0
    for i,line in enumerate(open(TSSFilename)):
        if line[0]=='S':continue
        if i%100==0:
            print i
        fields = line.replace('\n','').split('\t')
        if fields[3]=='+':
            name = fields[4]
            geneIndex[name]={}
            chromosome = fields[0]
            geneIndex[name]['chr']=chromosome
            start=int(fields[1])
            stop=int(fields[2])
            strand=fields[3]
            geneIndex[name]['strand']=fields[3]
            geneIndex[name]['start']=start
            geneIndex[name]['stop']=stop
            if chromosome == chr1:
                #read into the file to find the index of the gene TSS
                while lineNum1<start-1:
                    line1=trackFile1.readline()
                    lineNum1+=1
                geneIndex[name]['plus_start_index']=trackFile1.tell()
            else:
                while chromosome!=chr1:
                    while line1[0]!='c':
                        line1=trackFile1.readline()
                        
                    chr1=line1.replace('\n','').split('=')
                    chr1=chr1[1]
                    lineNum1 = 0
                while lineNum1<start-1:
                    line1=trackFile1.readline()
                    lineNum1+=1
                geneIndex[name]['plus_start_index']=trackFile1.tell()
             
            if chromosome == chr2:
                #read into the file to find the index of the gene TSS
                while lineNum2<start-1:
                    line2=trackFile2.readline()
                    lineNum2+=1
                geneIndex[name]['minus_start_index']=trackFile2.tell()
            else:
                while chromosome!=chr2:
                    while line2[0]!='c':
                        line2=trackFile2.readline()   
                    chr2=line2.replace('\n','').split('=')
                    chr2=chr2[1]
                    lineNum2 = 0
                while lineNum2<start-1:
                    line2=trackFile2.readline()
                    lineNum2+=1
                geneIndex[name]['minus_start_index']=trackFile2.tell()
        else:
            if strand == '+':
                #back up!
                trackFile1.seek(0)
                trackFile2.seek(0)
                chr1=trackFile1.readline().replace('\n','').split('=')
                chr1=chr1[1]
                lineNum1=0
                chr2=trackFile2.readline().replace('\n','').split('=')
                chr2=chr2[1]
                lineNum2=0    
            name = fields[4]
            geneIndex[name]={}
            chromosome = fields[0]
            geneIndex[name]['chr']=chromosome
            start=int(fields[1])
            stop=int(fields[2])
            strand = fields[3]
            geneIndex[name]['strand']=fields[3]
            geneIndex[name]['start']=start
            geneIndex[name]['stop']=stop
            if chromosome == chr1:
                #read into the file to find the index of the gene TSS
                while lineNum1<start-1:
                    line1=trackFile1.readline()
                    lineNum1+=1
                geneIndex[name]['plus_start_index']=trackFile1.tell()
            else:
                while chromosome!=chr1:
                    while line1[0]!='c':
                        line1=trackFile1.readline()
                        
                    chr1=line1.replace('\n','').split('=')
                    chr1=chr1[1]
                    lineNum1 = 0
                while lineNum1<start-1:
                    line1=trackFile1.readline()
                    lineNum1+=1
                geneIndex[name]['plus_start_index']=trackFile1.tell()
             
            if chromosome == chr2:
                #read into the file to find the index of the gene TSS
                while lineNum2<start-1:
                    line2=trackFile2.readline()
                    lineNum2+=1
                geneIndex[name]['minus_start_index']=trackFile2.tell()
            else:
                while chromosome!=chr2:
                    while line2[0]!='c':
                        line2=trackFile2.readline()   
                    chr2=line2.replace('\n','').split('=')
                    chr2=chr2[1]
                    lineNum2 = 0
                while lineNum2<start-1:
                    line2=trackFile2.readline()
                    lineNum2+=1
                geneIndex[name]['minus_start_index']=trackFile2.tell()
    
    
    
    filename = trackFilename+'_index.txt'
    outFile1= open(filename, 'w')
    
    
    #output format:
    # name  chr start   stop    strand  index+  index-
    for name in geneIndex:
        gene = geneIndex[name]
        outFile1.write('%s\t%s\t%s\t%s\t%s\t%s\t%s\n' % (name,gene['chr'],gene['start'],gene['stop'],gene['strand'],gene['plus_start_index'],gene['minus_start_index']))
    
    outFile1.close()
   
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
       
        
        
    
    


##############################################
if __name__ == "__main__":
    sys.exit(main())
