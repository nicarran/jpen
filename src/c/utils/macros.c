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
#include "macros.h"

/**
s1 must be: NULL, or have been obtained with malloc.
*/
char *mf_concat(char *s1, char *s2){
  int size=0;
  if(s2!=NULL)
	 size+=strlen(s2);
  if(s1!=NULL)
	 size+=strlen(s1);
  char *newS1=malloc((size+1)*sizeof(char));
  newS1[0]='\0';
  if(s1!=NULL){
	 strcpy(newS1, s1);
	 free(s1);
  }
  if(s2!=NULL)
	 strcat(newS1, s2);
  return newS1;
}
