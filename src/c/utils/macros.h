/* [{
* (C) Copyright 2007 Nicolas Carranza and individual contributors.
* See the jpen-copyright.txt file in the jpen distribution for a full
* listing of individual contributors.
*
* This file is part of jpen.
*
* jpen is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* jpen is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with jpen.  If not, see <http://www.gnu.org/licenses/>.
* }] */
#ifndef macros_h
#define macros_h

#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#define errorState -1
#define cleanState 0
#define true 1
#define false 0

extern char *mf_concat(char *s1, char *s2);

#define m_concat(s1, s2) s1 = mf_concat(s1, s2);
#define m_newstr(new, content) free(new); new=mf_concat(NULL, content);

#define m_declareRow(type) \
struct type##Cell {\
	struct type *pContent;\
	int prevCell;\
	int nextCell;\
};\
struct type##Row {\
	struct type##Cell *pCells;\
	int size;\
	int firstUsedCell;\
	int firstFreeCell;\
	int usedSize;\
	char *error;\
};\
typedef struct type S##type;\
typedef struct type##Row S##type##Row;\
typedef struct type##Cell S##type##Cell;\
extern struct type##Row type##_row;\
extern struct type##Cell * type##_getPCell(int cellIndex);\
extern struct type * type##_getP(int cellIndex);\
extern int type##_create(void);\
extern int type##_preCreate(struct type *pCreated);\
extern int type##_destroy(int cellIndex);\
extern int type##_preDestroy(struct type *pToDestroy);\
extern void type##_setError(char *error);\
extern void type##_appendError(char *error)

	#define m_implementRow(type) \
\
	struct type##Row type##_row;\
struct type##Cell * type##_getPCell(int cellIndex){\
	return type##_row.pCells+cellIndex;\
}\
\
struct type * type##_getP(int cellIndex){\
	return  type##_getPCell(cellIndex)->pContent;\
}\
\
int type##_create(void){\
	if(type##_row.size==0){\
		type##_row.firstUsedCell=-1;\
		type##_row.firstFreeCell=-1;\
	}\
	struct type *pNew=calloc(1, sizeof(struct type));\
	if(!pNew){\
		type##_setError("Insuficient memory to create new "#type ".");\
		return -1;\
	}\
	if(type##_preCreate(pNew)){\
		type##_setError("Initialization of created "#type" failed.");\
		free(pNew);\
		return -1;\
	}\
	int cellIndex=type##_row.firstFreeCell;\
	int reusing=1;\
	if(cellIndex==-1){\
		reusing=0;\
		struct type##Cell *pNewCells=realloc(type##_row.pCells,\
																							 (type##_row.size+1)*sizeof(struct type##Cell));\
	if(!pNewCells){\
			type##_setError("Insuficient memory to reallocate for "#type".");\
			free(pNew);\
			return -1;\
		}\
		type##_row.pCells=pNewCells;\
		cellIndex=type##_row.size++;\
	}\
	struct type##Cell *pCell=type##_getPCell(cellIndex);\
	if(reusing){\
		type##_row.firstFreeCell=pCell->nextCell;\
	}\
	pNew->cellIndex=cellIndex;\
	pCell->pContent=pNew;\
	pCell->prevCell=-1;\
	pCell->nextCell=type##_row.firstUsedCell;\
	if(pCell->nextCell!=-1){\
		struct type##Cell *pNextUsedCell=type##_getPCell(pCell->nextCell);\
		pNextUsedCell->prevCell=cellIndex;\
	}\
	type##_row.firstUsedCell=cellIndex;\
	type##_row.usedSize++;\
	return cellIndex;\
}\
\
int type##_destroy(int cellIndex){\
	if(cellIndex<0 || cellIndex>=type##_row.size){\
		type##_setError("cellIndex out of range.");\
		return -1;\
	}\
	struct type##Cell *pCell=type##_getPCell(cellIndex);\
	if(pCell->pContent==NULL){\
		type##_setError("Cell content already destroyed.");\
		return -1;\
	}\
	if(type##_preDestroy(pCell->pContent)){\
		type##_setError("preDestroy failed.");\
		return -1;\
	}\
	if(pCell->prevCell==-1 && pCell->nextCell==-1 ){\
		type##_row.firstUsedCell=-1;\
	}else if(pCell->prevCell==-1){\
		struct type##Cell *pNextCell=type##_getPCell(pCell->nextCell);\
		pNextCell->prevCell=-1;\
		type##_row.firstUsedCell=pCell->nextCell;\
	}else if(pCell->nextCell==-1){\
		struct type##Cell *pPrevCell=type##_getPCell(pCell->prevCell);\
		pPrevCell->nextCell=-1;\
	}else{\
		struct type##Cell *pPrevCell=type##_getPCell(pCell->prevCell);\
		struct type##Cell *pNextCell=type##_getPCell(pCell->nextCell);\
		pPrevCell->nextCell=pCell->nextCell;\
		pNextCell->prevCell=pCell->prevCell;\
	}\
	free(pCell->pContent);\
	pCell->pContent=NULL;\
	pCell->prevCell=-1;\
	pCell->nextCell=type##_row.firstFreeCell;\
	type##_row.firstFreeCell=cellIndex;\
	type##_row.usedSize--;\
	return 0;\
}\
\
void type##_setError(char *error){\
	printf("--- jni: "#type": %s \n", error);\
	m_newstr(type##_row.error, #type ": ");\
	type##_appendError(error);\
}\
\
void type##_appendError(char *toAppend){\
	m_concat(type##_row.error, toAppend);\
}

#endif
