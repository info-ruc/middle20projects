#include "db.h"
#include "utils.h"
#include <iostream>
#include <unistd.h>
#include <fcntl.h>
#include <sys/poll.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <time.h>
#include <sys/types.h>
#include <string.h>

#define MAXLINE       128
#define NEWLINE       '\n'

/*
 * A global variable, record the send record
 */
SendData sdata;

char* trimcpy(char dest[], int nsize, char src[]);
int inputParser(char sendline[]);
void wait_verify(int connfd);
int cmd_operation(char* cmd, int connfd);
int judge_access();


int
main(int argc, char **argv)
{
	int					sockfd, n;
	char				recvline[MAXLINE + 1], *sendline = NULL;
	struct sockaddr_in	servaddr;
    char                server_ip[IPLEN];
    int                 port = 23790;
    size_t              len = 0;

    /*
     * TCP connect to server
     */
	if (argc != 2) {
        strcpy(server_ip, "127.0.0.1");
    }
	if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("main: socket error");
        exit(1);
    }

	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port   = htons(port);	/* daytime server */
	if (inet_pton(AF_INET, server_ip, &servaddr.sin_addr) <= 0) {
		perror("inet_pton error for server ip.");
		exit(1);
    }

	if (connect(sockfd, (SA *) &servaddr, sizeof(servaddr)) < 0) {
		perror("connect error");
		exit(1);
    }

    /*
     * Wait for user's name && passwd input
     */
    wait_verify(sockfd);
    getchar();
    /*
     * Set the default dbname and accessbits
     */
    strncpy(sdata.dbname, "default", MAXDBNAMELEN);
    sdata.accessbits = (1 << 3) + (1 << 2) + (1 << 1) + 1;

    printf("%s:%d#%s>", server_ip, port, sdata.dbname);
	while ( (len = getline(&sendline, &len, stdin)) > 0 ) {
        if(inputParser(sendline) < 0) {
            printf("%s:%d#%s>", server_ip, port, sdata.dbname);
            continue;
        }

        if(judge_access() < 0) {
            printf("(ERROR) you dont have the priviledge.\n");
            continue;
        }

        cmd_operation(sdata.cmd, sockfd);

		printf("%s:%d#%s>", server_ip, port, sdata.dbname);
	}

	free(sendline);

	exit(0);
}

int judge_access()
{
    /*
     * Judge if one has its access
     */
    if (strcmp(sdata.cmd, "grant") == 0 && strcmp(sdata.cmd, "revoke") == 0) {
        int accessbit = 0;
        if (strcmp(sdata.key, "get") == 0) accessbit |= (1 << 3);
        else if (strcmp(sdata.key, "set") == 0) accessbit |= (1 << 2);
        else if (strcmp(sdata.key, "repl") == 0) accessbit |= (1 << 1);
        else accessbit |= 1;

        if (!(sdata.accessbits | accessbit)) {
            return -3;
        }
    }

    else if (strcmp(sdata.cmd, "get") == 0   ||
             strcmp(sdata.cmd, "set") == 0   ||
             strcmp(sdata.cmd, "repl") == 0 ||
             strcmp(sdata.cmd, "del") == 0) {
        if ( !(strcmp(sdata.cmd, "get") == 0 && (sdata.accessbits | (1 << 3))) &&
                  !(strcmp(sdata.cmd, "set") == 0 && (sdata.accessbits | (1 << 2))) &&
                  !(strcmp(sdata.cmd, "repl") == 0 && (sdata.accessbits | (1 << 1))) &&
                  !(strcmp(sdata.cmd, "del") == 0 && (sdata.accessbits | 1))) {
            return -1;
        }
    }

    return 0;
}


char* trimcpy(char dest[], int nsize, char src[]) {
    while (*src == SPACE || *src == NEWLINE) src++; // remove the front space

    int len = 0;
    char* sp = src;
    while (*src != 0 && *src != SPACE && *src != NEWLINE) {
        src++;
        len++;
    }
    if (len >= nsize || len == 0) return NULL;
    strncpy(dest, sp, len);
    dest[len] = 0;

    return src;
}

int inputParser(char sendline[]) {
    /* parse the cmd */
    char* src = sendline;
    if ( (src = trimcpy(sdata.cmd, MAXCMDLEN, src) ) == NULL ) {
        printf("(ERROR) cmd not exist or meaningful\n");
        return -1;
    }
    if (strcmp(sdata.cmd, "get")    != 0 &&
        strcmp(sdata.cmd, "set")    != 0 &&
        strcmp(sdata.cmd, "repl")   != 0 &&
        strcmp(sdata.cmd, "del")    != 0 &&
        strcmp(sdata.cmd, "exit")   != 0 &&
        strcmp(sdata.cmd, "use")    != 0 &&
        strcmp(sdata.cmd, "ctable") != 0 &&
        strcmp(sdata.cmd, "grant")  != 0 &&
        strcmp(sdata.cmd, "revoke") != 0) {
            printf("(ERROR) cmd not exist\n");
            return -2;
    }

    if (strcmp(sdata.cmd, "exit") == 0) return 0;

    /* parse the key */
    if ( (src = trimcpy(sdata.key, MAXKEYLEN, src)) == NULL ) {
        printf("(ERROR) key not OK\n");
        return -3;
    }
    if (strcmp(sdata.cmd, "grant") == 0 && strcmp(sdata.cmd, "revoke") == 0) {
        if (strcmp(sdata.key, "get")  != 0 &&
            strcmp(sdata.key, "set")  != 0 &&
            strcmp(sdata.key, "del")  != 0 &&
            strcmp(sdata.key, "repl") != 0 ) {
            printf("(ERROR) key not a cmd\n");
            return -3;
        }
    }

    /* Parse the value if exists */
    if (strcmp(sdata.cmd, "set") == 0 || strcmp(sdata.cmd, "repl") == 0 ||
        strcmp(sdata.cmd, "grant") == 0 || strcmp(sdata.cmd, "revoke") == 0) {
        if ( (src = trimcpy(sdata.value, MAXVALUELEN, src)) == NULL ) {
            printf("(ERROR) value not OK\n");
            return -4;
        }
    }

    /* Parse the grant and revoke */
    if (strcmp(sdata.cmd, "grant") == 0 || strcmp(sdata.cmd, "revoke") == 0) {
        if ( (src = trimcpy(sdata.value2, MAXVALUELEN, src)) == NULL ) {
            printf("(ERROR) value2 not OK\n");
            return -5;
        }
    }

    return 0;
}


void wait_verify(int connfd)
{
    while (true) {
        printf("Please input your username & password~\n");
        /*
         * Login input and then judge.
         */
        strcpy(sdata.cmd, "login");
        printf("USERNAME: ");
        scanf("%s", sdata.user);
        printf("PASSWORD: ");
        scanf("%s", sdata.passwd);

        int len = sizeof(sdata);
        int n;
        if( (n = writen(connfd, (char*)&sdata, len)) != len ) {
            perror("main: writen error");
            exit(1);
        }

        RcvData rdata;
        if( (n = readn(connfd, (char*)&rdata, sizeof(RcvData))) != sizeof(RcvData) ) {
            perror("main: readn error");
            exit(1);
        }

        /*
         * judge if verified, 0 is OK
         */
        printf("%s\n", rdata.result);
        if (rdata.id == 0)
            break;
	}
}

int cmd_operation(char* cmd, int connfd)
{
    int n;

    /*
     * 'exit', exit the client
     */
    if (strcmp(cmd, "exit") == 0) {
        printf("Byebye~\n");
        exit(0);   /* exit the client */
    }

    int len = sizeof(sdata);
    if( (n = writen(connfd, (char*)&sdata, len)) != len ) {
        perror("main: write error");
        exit(1);
    }

    RcvData rdata;
    len = sizeof(rdata);
    if( (n = readn(connfd, (char*)&rdata, sizeof(RcvData))) != len ) {
        perror("main: write error");
        exit(1);
	}

	/*
     * If success, alter the database
     */
    if (strcmp(cmd, "use") == 0 && rdata.id == 0) {
        strncpy(sdata.dbname, sdata.key, MAXDBNAMELEN);
        sdata.accessbits = rdata.accessbits;
    }

    if (fputs(rdata.result, stdout) == EOF) {
        perror("main: fputs error");
        exit(1);
    }


    return 0;
}

