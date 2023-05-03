public class MIDIUtil {

  private static String[][] NOTES = new String[][] {
      new String[] { "C" },
      new String[] { "Db", "C#" },
      new String[] { "D", },
      new String[] { "Eb", "D#" },
      new String[] { "E", },
      new String[] { "F", },
      new String[] { "Gb", "F#" },
      new String[] { "G" },
      new String[] { "Ab", "G#" },
      new String[] { "A" },
      new String[] { "Bb", "A#" },
      new String[] { "B" },
  };

  public static int toMIDI(String note) {
    for (var i = 0; i < NOTES.length; ++i) {
      for (var noteCandidate : NOTES[i]) {
        if (note.startsWith(noteCandidate)) {
          var offset = i;
          var octave = Integer.parseInt(note.replaceFirst(noteCandidate, ""));
          return offset + (octave + 1) * 12;
        }
      }
    }
    throw new IllegalArgumentException("Expected a note in scientific pitch notation, got " + note);
  }

  public static String fromMidi(int number) {
    var offset = number % 12;
    var octave = (number - offset) / 12;
    return NOTES[offset][0] + (octave - 1);
  }
}
