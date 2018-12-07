import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
    selector: 'mico-yes-no-dialog',
    templateUrl: './yes-no-dialog.component.html',
    styleUrls: ['./yes-no-dialog.component.css']
})
export class YesNoDialogComponent implements OnInit {

    object: String;
    question: String;

    constructor(public dialogRef: MatDialogRef<YesNoDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
        this.object = data.object;
        this.question = data.question;
    }

    ngOnInit() {
    }

    input() {
        return true;
    }

}
