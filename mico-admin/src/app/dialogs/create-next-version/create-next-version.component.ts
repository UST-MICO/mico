import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { incrementVersion, versionComponents } from 'src/app/api/semantic-version';

@Component({
    selector: 'mico-create-next-version',
    templateUrl: './create-next-version.component.html',
    styleUrls: ['./create-next-version.component.css']
})
export class CreateNextVersionComponent implements OnInit {

    major;
    minor;
    patch;

    picked;

    constructor(
        public dialogRef: MatDialogRef<CreateNextVersionComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any
    ) {
        const version = data.version;

        this.major = incrementVersion(version, versionComponents.major);
        this.minor = incrementVersion(version, versionComponents.minor);
        this.patch = incrementVersion(version, versionComponents.patch);

    }

    ngOnInit() {
    }

    confirm() {
        return this.picked;
    }
}
